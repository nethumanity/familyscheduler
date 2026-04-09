package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.RequirementToggleOverride
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis

object AvailabilityEngine {

    data class SlotIndex(
        val byIndex: Map<Int, List<Int>>,
        val byPersonIndex: Map<Pair<Person, Int>, Int>
    )

    fun recompute(
        originalSlots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>,
        overrides: List<RequirementOverride>
    ): AvailabilityResult {

        val workingSlots =
            originalSlots.map { it.copy() }.toMutableList()

        val index = buildSlotIndex(workingSlots)

        assignHouseholdTasks(
            slots = workingSlots,
            slotIndex = index,
            requirements = requirements,
            overrides = overrides
        )

        assignRemainingUnassignedToFree(workingSlots)

        val evaluations =
            evaluateAvailability(
                slots = workingSlots,
                slotIndex = index,
                requirements = requirements
            )

        return AvailabilityResult(
            slots = workingSlots,
            evaluations = evaluations
        )
    }

    fun buildSlotIndex(slots: List<TimeSlot>): SlotIndex {
        val byIndex = slots.indices.groupBy { slots[it].index }

        val byPersonIndex =
            slots.indices.associateBy { i ->
                slots[i].person to slots[i].index
            }

        return SlotIndex(byIndex, byPersonIndex)
    }

    fun assignHouseholdTasks(
        slots: MutableList<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        overrides: List<RequirementOverride>
    ) {

        internalAssign(
            slots = slots,
            slotIndex = slotIndex,
            requirements = requirements
        )

        applyReverseOverrides(
            slots = slots,
            slotIndex = slotIndex,
            requirements = requirements,
            overrides = overrides
        )
    }

    private fun internalAssign(
        slots: MutableList<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>?
    ) {
        if (requirements.isNullOrEmpty()) return

        val orderedReqs = requirements.sortedByDescending {
            it.prioritySeed
        }

        for (req in orderedReqs) {
            for (index in TimeAxis.indices) {
                if (!req.isRequiredAt(index)) continue

                val indicesAtIndex = slotIndex.byIndex[index].orEmpty()

                val alreadySatisfied =
                    indicesAtIndex.filter { i ->
                        val slot = slots[i]
                        slot.person in req.allowedPersons &&
                                slot.state == req.targetState
                    }

                for (i in alreadySatisfied) {
                    val slot = slots[i]
                    slots[i] = slot.copy(
                        taskName = slot.taskName + req.name
                    )
                }

                var remaining = req.requiredCount - alreadySatisfied.size
                if (remaining <= 0) continue

                val candidates =
                    indicesAtIndex.filter { i ->
                        val slot = slots[i]
                        slot.person in req.allowedPersons &&
                                slot.state == SlotState.UNASSIGNED
                    } //.sortedBy { it.person }   //必要に応じて、誰から割り当てるかを実装

                for (i in candidates) {
                    if (remaining <= 0) break

                    val slot = slots[i]
                    slots[i] = slot.copy(
                        state = req.targetState,
                        taskName = slot.taskName + req.name
                    )
                    remaining--
                }
            }
        }
    }

    private fun applyReverseOverrides(
        slots: MutableList<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        overrides: List<RequirementOverride>
    ) {
        val reverseRuleIds = overrides
            .filterIsInstance<RequirementToggleOverride>()
            .filter { it.mode == RequirementModeToday.REVERSE }
            .map { it.ruleId }
            .toSet()

        if (reverseRuleIds.isEmpty()) return

        requirements
            .filterIsInstance<TimeRangeHouseholdRequirement>()
            .filter { it.sourceRuleId in reverseRuleIds }
            .forEach { req ->

                for (index in TimeAxis.indices) {

                    if (!req.isRequiredAt(index)) continue

                    val indicesAtIndex = slotIndex.byIndex[index].orEmpty()

                    // ① 今の割当を取得
                    val assigned =
                        indicesAtIndex.filter { i ->
                            val slot = slots[i]
                            slot.person in req.allowedPersons &&
                                    slot.state == req.targetState &&
                                    req.name in slot.taskName
                    }

                    // ② 反転対象
                    val assignedPersons = assigned.map { i -> slots[i].person }.toSet()
                    val reversedPersons = req.allowedPersons - assignedPersons

                    // ③ 一旦削除
                    assigned.forEach { i ->
                        val slot = slots[i]
                        slots[i] = slot.copy(
                            state = SlotState.UNASSIGNED,
                            taskName = slot.taskName - req.name
                        )
                    }

                    // ④ 強制assign（軽いガードのみ）
                    reversedPersons.forEach { person ->

                        val candidate =
                            indicesAtIndex
                            .firstOrNull { i ->
                                val slot = slots[i]
                                slot.person == person &&
                                        slot.state.weight <= req.targetState.weight
                            }

                        if (candidate != null) {
                            val slot = slots[candidate]

                            slots[candidate] = slot.copy(
                                state = req.targetState,
                                taskName = slot.taskName + req.name
                            )
                        }
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
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>
    ): List<AvailabilityEvaluation> {

        return TimeAxis.indices.mapNotNull { index ->

            val slotsAtIndex = slotIndex.byIndex[index].orEmpty().map { slots[it] }

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
                        MissingReason(
                            sourceRuleId = req.sourceRuleId,
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
                    slotIndex = slotIndex,
                    requirements = activeReqs, //GPTの推奨はrequirements,関数内でインデックスフィルター
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

    fun collectBlockInfo(   // BlockInfoは単数前提に変更
        slots: List<TimeSlot>,
        persons: List<Person>,
    ): BlockInfo {

        val slotByPerson = slots.filter { it.person in persons }

        return BlockInfo(
                person = persons,
                currentState = slotByPerson.map { it.state },
                taskName = slotByPerson.flatMap { it.taskName }
        )
    }

    fun flexResolveProposalsAt(     //このblockは動かす候補になるか？（インデックスフィルターなし）
        index: Int,
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        reasons: List<MissingReason>
    ): List<FlexResolveProposal> {

        val hasFlexRequirement =
            requirements.any {
                it.flexWindowSlots.backward != 0 ||
                        it.flexWindowSlots.forward != 0
            }

        if (!hasFlexRequirement) return emptyList()

        return reasons
            .flatMap { reason ->
                generateFlexResolveProposalsForReason(
                    index = index,
                    slots = slots,
                    slotIndex = slotIndex,
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
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        reason: MissingReason
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
                canAssign(
                    person = it,
                    candidateIndex = candidateIndex,
                    slots =slots,
                    slotIndex = slotIndex,
                    requiredState = requirement.targetState
                )
            }

            when {
                // ★ 1人必要 → 全員分 proposal
                requirement.requiredCount == 1 -> {
                    validPersons.map { person ->
                        FlexResolveProposal(
                            sourceRuleId = requirement.sourceRuleId,
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
                            sourceRuleId = requirement.sourceRuleId,
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

    fun canAssign(  //その場所に置けるか？
        person: Person,
        candidateIndex: Int,
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requiredState: SlotState
    ): Boolean {
        val i = slotIndex.byPersonIndex[person to candidateIndex]
            ?: return false

        val slot = slots[i]
        val candidatePriority = slot.state.weight
        val reqPriority = requiredState.weight

        return candidatePriority <= reqPriority
    }
}
