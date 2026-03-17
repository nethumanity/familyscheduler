package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis

object AvailabilityEngine {

    fun recompute(
        originalSlots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>
    ): AvailabilityResult {

        /*
        if (requirements.isEmpty()) {
            return AvailabilityResult(
                slots = originalSlots,
                evaluations = emptyList()
            )
        }
         */

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

    //割り当て項
    fun assignHouseholdTasks(
        slots: MutableList<TimeSlot>,
        requirements: List<HouseholdRequirement>?
    ) {
        if (requirements.isNullOrEmpty()) return
        // FIX → FLEX
        val orderedReqs = requirements.sortedBy {
            if (it.flexWindowSlots.backward == 0 && it.flexWindowSlots.forward == 0) 0 else 1
        }

        for (req in orderedReqs) {
            for (index in TimeAxis.indices) {
                if (!req.isRequiredAt(index)) continue

                val slotsAtIndex = slots.filter { it.index == index }

                val alreadySatisfied =
                    slotsAtIndex.count {
                        it.person in req.allowedPersons &&
                                it.state == req.targetState
                    }

                var remaining = req.requiredCount - alreadySatisfied
                if (remaining <= 0) continue

                val candidates =
                    slotsAtIndex
                        .filter {
                            it.person in req.allowedPersons &&
                                    it.state == SlotState.UNASSIGNED
                        }

                for (slot in candidates) {
                    if (remaining <= 0) break

                    val i = slots.indexOf(slot)
                    slots[i] = slot.copy(state = req.targetState,
                        flexWindow = FlexWindowParameters(
                            backward = req.flexWindowSlots.backward,
                            forward = req.flexWindowSlots.forward
                        ),
                        taskName = req.name
                    )
                    remaining--
                }
            }
        }

        //assignRemainingUnassignedToFree(slots)
    }

    private fun assignRemainingUnassignedToFree(
        slots: MutableList<TimeSlot>
    ) {
        for (i in slots.indices) {
            val slot = slots[i]

            if (slot.state == SlotState.UNASSIGNED) {
                slots[i] = slot.copy(
                    state = SlotState.FREE  //必要ならflexWindow = 0,taskName = nullを追加
                )
            }
        }
    }

    //評価項
    fun evaluateAvailability(
        slots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>
    ): List<AvailabilityEvaluation> {

        return TimeAxis.indices.map { index ->

            val slotsAtIndex = slots.filter { it.index == index }
            val activeReqs = requirements.filter { it.isRequiredAt(index) }

            val reasons = mutableListOf<MissingReason>()

            activeReqs.forEach { req ->

                val satisfiedCount =
                    slotsAtIndex.count { slot ->
                        slot.person in req.allowedPersons &&
                                slot.state == req.targetState
                    }

                val required = req.requiredCount
                val assigned = satisfiedCount
                val missing = required - assigned

                if (missing > 0) {
                    val info = collectBlockInfo(slotsAtIndex, req)
                    reasons.add(
                        MissingReason.NotEnoughPeople(
                            requirementName = req.name,
                            requiredCount = required,
                            assignedCount = assigned,
                            blockingPersons = info
                        )
                    )
                }

                if (assigned == 0 && required > 0 && req.allowedPersons.isEmpty()) {
                    reasons.add(
                        MissingReason.NoAssignablePerson(
                            requirementName = req.name
                        )
                    )
                }
            }

            val totalRequired = activeReqs.sumOf { it.requiredCount }
            val totalAssigned =
                activeReqs.sumOf { req ->
                    slotsAtIndex.count { slot ->
                        slot.person in req.allowedPersons &&
                                slot.state == req.targetState
                    }.coerceAtMost(req.requiredCount)
                }

            val proposals =
                flexResolveProposalsAt(
                    index = index,
                    slots = slots,
                    requirements = requirements
                )

            AvailabilityEvaluation(
                index = index,
                requiredCount = activeReqs.sumOf { it.requiredCount },
                availableCount = slotsAtIndex.count { it.state == SlotState.FREE },
                hasFixRequirement = activeReqs.any { it.flexWindowSlots.backward == 0 && it.flexWindowSlots.forward == 0 },
                hasFlexRequirement = activeReqs.any { it.flexWindowSlots.backward != 0 || it.flexWindowSlots.forward != 0 },
                missing = reasons.size,
                reasons = reasons,
                flexProposals = proposals   // ←ここが重要
            )
        }
    }

    fun collectBlockInfo(
        slots: List<TimeSlot>,
        requirement: HouseholdRequirement
    ): List<BlockInfo> {
        val persons = requirement.allowedPersons
        val currentState = slots
            .filter { it.person in persons }
            .map { it.state }
        val taskName = requirement.name

        return listOf(
            BlockInfo(
                person = persons.toList(),
                currentState = currentState,
                taskName = taskName
            )
        )
    }

    fun flexResolveProposalsAt(
        index: Int,
        slots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>
    ): List<FlexResolveProposal> {

        val slotsAtIndex = slots.filter { it.index == index }

        val activeReqs = requirements.filter { it.isRequiredAt(index) }

        val reasons = mutableListOf<MissingReason>()

        activeReqs.forEach { req ->

            val assigned = slotsAtIndex.count {
                it.person in req.allowedPersons &&
                        it.state == req.targetState
            }

            val missing = req.requiredCount - assigned

            if (missing > 0) {
                reasons.add(
                    MissingReason.NotEnoughPeople(
                        requirementName = req.name,
                        requiredCount = req.requiredCount,
                        assignedCount = assigned,
                        blockingPersons = emptyList()
                    )
                )
            }
        }

        val hasFlexRequirement =
            activeReqs.any {
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
    }

    fun generateFlexResolveProposalsForReason(
        index: Int,
        slots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>,
        reason: MissingReason.NotEnoughPeople
    ): List<FlexResolveProposal> {

        val persons = listOf(
            Person.FATHER,
            Person.MOTHER
        )
        val requirement = requirements
            .find { it.name == reason.requirementName }
            ?: return emptyList()
        val window = requirement.flexWindowSlots
        val offsets = (-window.backward until 0) + (1..window.forward)

        return offsets.flatMap { offset ->
            val candidateIndex = index + offset
            if (candidateIndex !in TimeAxis.all.indices) return@flatMap emptyList()

            persons.mapNotNull { person ->

                // ① 移動元にその人が存在するか
                val candidateSlot = slots.find {
                    it.person == person && it.index == candidateIndex
                } ?: return@mapNotNull null

                // ② 移動元が動かせる状態か　←いらない？？
                //if (candidateSlot.state !in listOf(SlotState.FREE, SlotState.LIFE))
                //    return@mapNotNull null

                // ③ 移動先で requirement を満たせるか
                if (!canAssign(person, index, slots,requirement.targetState))
                    return@mapNotNull null

                FlexResolveProposal(
                    requirementName = reason.requirementName,
                    person = person,
                    candidateIndex = candidateIndex,
                    initialIndex = index,
                    deltaMinutes = offset * TimeAxis.stepMinutes,
                    targetState = requirement.targetState
                )
            }
        }
    }

    private fun canAssign(
        person: Person,
        index: Int,
        slots: List<TimeSlot>,
        requiredState: SlotState
    ): Boolean {
        val slot = slots.find { it.person == person && it.index == index }
            ?: return false
        val candidatePriority = slot.state.weight
        val initialPriority = requiredState.weight

        return candidatePriority <= initialPriority
    }
}
