package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis

object AvailabilityEngine {

    fun recompute(
        originalSlots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>
    ): AvailabilityResult {

        val workingSlots =
            originalSlots.map { it.copy() }.toMutableList()

        assignHouseholdTasks(
            slots = workingSlots,
            requirements = requirements
        )

        assignRemainingUnassignedToFree(workingSlots)

        val evaluations =
            evaluateAvailability(
                slots = workingSlots,
                requirements = requirements
            )

        return AvailabilityResult(
            slots = workingSlots,
            evaluations = evaluations
        )
    }

    fun assignHouseholdTasks(
        slots: MutableList<TimeSlot>,
        requirements: List<HouseholdRequirement>?
    ) {
        if (requirements.isNullOrEmpty()) return

        val orderedReqs = requirements.sortedByDescending {
            it.prioritySeed
        }

        val slotsByIndex = slots.withIndex().groupBy { it.value.index }

        for (req in orderedReqs) {
            for (index in TimeAxis.indices) {
                if (!req.isRequiredAt(index)) continue

                //val slotsAtIndex = slots.filter { it.index == index }
                val slotsAtIndex = slotsByIndex[index].orEmpty() //O(n2)対応→たぶんOK

                val alreadySatisfiedSlots =
                    slotsAtIndex.filter { (_, slot) ->
                        slot.person in req.allowedPersons &&
                                slot.state == req.targetState
                    }

                val alreadySatisfied = alreadySatisfiedSlots.size

                for ((i, slot) in alreadySatisfiedSlots) {
                    slots[i] = slot.copy(
                        taskName = slot.taskName + req.name
                    )
                }

                var remaining = req.requiredCount - alreadySatisfied
                if (remaining <= 0) continue

                val candidates =
                    slotsAtIndex.filter { (_, slot) ->
                            slot.person in req.allowedPersons &&
                                    slot.state == SlotState.UNASSIGNED
                        }
                        //.sortedBy { it.person }   //誰から割り当てるかが未定義

                for ((i, slot) in candidates) {
                    if (remaining <= 0) break

                    //val i = slots.indexOf(slot)
                    slots[i] = slot.copy(
                        state = req.targetState,
                        flexWindow = req.flexWindowSlots,
                        taskName = slot.taskName + req.name //念のため追加する形
                    )
                    remaining--
                }
            }
        }
    }

    private fun assignRemainingUnassignedToFree(
        slots: MutableList<TimeSlot>
    ) {
        for (i in slots.indices) {
            val slot = slots[i]

            if (slot.state == SlotState.UNASSIGNED) {
                slots[i] = slot.copy(
                    state = SlotState.FREE  //flexWindowParameters(0, 0)であることに留意
                )
            }
        }
    }

    fun evaluateAvailability(
        slots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>
    ): List<AvailabilityEvaluation> {

        val slotsByIndex = slots.groupBy { it.index }

        return TimeAxis.indices.mapNotNull { index ->

            //val slotsAtIndex = slots.filter { it.index == index }   //O(n2)問題に対応
            val slotsAtIndex = slotsByIndex[index].orEmpty()
            val activeReqs = requirements.filter { it.isRequiredAt(index) }

            if (activeReqs.isEmpty()) return@mapNotNull null

            val reasons = mutableListOf<MissingReason>()

            activeReqs.forEach { req ->

                val satisfiedPersons =
                    slotsAtIndex
                        .filter { slot ->
                            slot.person in req.allowedPersons &&
                                    slot.state == req.targetState
                        }
                        .map { it.person }

                val required = req.requiredCount
                val assigned = satisfiedPersons.size
                val missing = required - assigned

                if (missing > 0) {
                    val info =
                        collectBlockInfo(
                            slots = slotsAtIndex,
                            persons = req.allowedPersons.toList() - satisfiedPersons
                        )
                    reasons.add(
                        MissingReason.NotEnoughPeople(
                            requirementName = req.name,
                            requiredCount = required,
                            assignedCount = assigned,
                            blockingPersons = info
                        )
                    )
                }
            }

            val proposals =
                flexResolveProposalsAt(
                    index = index,
                    slots = slots,
                    requirements = activeReqs, //GPTの推奨はrequirements,
                    reasons = reasons
                )

            AvailabilityEvaluation(
                index = index,
                hasFlexRequirement = activeReqs.any { it.flexWindowSlots.backward != 0 || it.flexWindowSlots.forward != 0 },
                missing = reasons.size,
                reasons = reasons,
                flexProposals = proposals
            )
        }
    }

    fun collectBlockInfo(
        slots: List<TimeSlot>,
        persons: List<Person>,
        //requirement: HouseholdRequirement
    ): List<BlockInfo> {
        //val persons = requirement.allowedPersons
        val slotByPerson = slots.filter { it.person in persons }

        return listOf(
            BlockInfo(
                person = persons,
                currentState = slotByPerson.map { it.state },
                taskName = slotByPerson.flatMap { it.taskName }
            )
        )
    }

    fun flexResolveProposalsAt(     //このblockは動かす候補になるか？
        index: Int,
        slots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>,
        reasons: List<MissingReason>
    ): List<FlexResolveProposal> {

        //val activeReqs = requirements.filter { it.isRequiredAt(index) }

        val hasFlexRequirement =
            //activeReqs.any {
            requirements.any {
                it.flexWindowSlots.backward != 0 ||
                        it.flexWindowSlots.forward != 0
            }

        if (!hasFlexRequirement) return emptyList()

        return reasons
            .filterIsInstance<MissingReason.NotEnoughPeople>()
            .flatMap { reason ->
                generateFlexResolveProposalsForReason(
                    index = index,
                    slots = slots,
                    requirements = requirements,
                    reason = reason
                )
            }
            .sortedBy { it.score(slots) }
            .take(3)
    }

    fun generateFlexResolveProposalsForReason(  //どこに動かせば解決しそうか？
        index: Int,
        slots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>,
        reason: MissingReason.NotEnoughPeople
    ): List<FlexResolveProposal> {

        val requirement = requirements
            .find { it.name == reason.requirementName }
            ?: return emptyList()

        val persons = requirement.allowedPersons.toList()
        val window = requirement.flexWindowSlots
        val offsets = (-window.backward until 0) + (1..window.forward)

        return offsets.flatMap { offset ->

            val candidateIndex = index + offset
            if (candidateIndex !in TimeAxis.all.indices) return@flatMap emptyList()

            val validPersons = persons.filter {
                canAssign(it, candidateIndex, slots, requirement.targetState)
            }

            when {
                // ★ 1人必要 → 全員分 proposal
                requirement.requiredCount == 1 -> {
                    validPersons.map { person ->
                        FlexResolveProposal(
                            requirementName = reason.requirementName,
                            persons = listOf(person),
                            initialIndex = index,
                            candidateIndex = candidateIndex,
                            targetState = requirement.targetState
                        )
                    }
                }

                // ★ 複数人必要 → まとめて1 proposal
                validPersons.size >= requirement.requiredCount -> {
                    listOf(
                        FlexResolveProposal(
                            requirementName = reason.requirementName,
                            persons = validPersons.take(requirement.requiredCount),
                            initialIndex = index,
                            candidateIndex = candidateIndex,
                            targetState = requirement.targetState
                        )
                    )
                }

                else -> emptyList()
            }
        }
    }

    private fun canAssign(  //その場所に置けるか？
        person: Person,
        candidateIndex: Int,
        slots: List<TimeSlot>,
        requiredState: SlotState
    ): Boolean {
        val slotsByPersonIndex = slots.associateBy { it.person to it.index }
        //val slot = slots.find { it.person == person && it.index == candidateIndex }
        val slot = slotsByPersonIndex[person to candidateIndex]  //O(n2)対応
            ?: return false

        val candidatePriority = slot.state.weight
        val reqPriority = requiredState.weight

        return candidatePriority <= reqPriority
    }
}
