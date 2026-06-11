package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.RequirementToggleOverride
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import kotlin.collections.firstOrNull
import kotlin.collections.orEmpty

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

        val warningReqs =
            assignHouseholdTasks(
                slots = workingSlots,
                slotIndex = index,
                requirements = requirements,
                overrides = overrides
            )

        val evaluations =
            evaluateAvailability(
                requirements = requirements,
                warningReqs = warningReqs
            )

        val proposals =
            warningReqs.flatMap { warningReq ->
                generateProposals(
                    slots = workingSlots,
                    slotIndex = index,
                    requirements = requirements,
                    warningReq = warningReq
                )
            }


        assignRemainingUnassignedToFree(workingSlots)

        return AvailabilityResult(
            slots = workingSlots,
            evaluations = evaluations,
            proposalsByRequirementId = proposals.groupBy { it.resolvedRequirementId }
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

    private fun assignHouseholdTasks(
        slots: MutableList<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        overrides: List<RequirementOverride>
    ): List<HouseholdRequirement> {
        val warnings =
            mutableListOf<HouseholdRequirement>()

        val orderedReqs = requirements.sortedByDescending {
            it.prioritySeed
        }

        val reverseRuleIds = overrides
            .filterIsInstance<RequirementToggleOverride>()
            .filter { it.mode == RequirementModeToday.REVERSE }
            .map { it.ruleId }
            .toSet()

        for (req in orderedReqs) {
            if (!mayAssignBlock(req, slots, slotIndex)) {
                warnings += req
            } else {
                assignBlock(req, slots, slotIndex, reverseRuleIds)
            }

        }
        return warnings
    }

    private fun assignBlock(
        req: HouseholdRequirement,
        slots: MutableList<TimeSlot>,
        slotIndex: SlotIndex,
        reverseRuleIds: Set<String>
    ) {
        val indices = req.allIndices()

        val candidates = req.allowedPersons
            .map { person ->

                val score = scorePersonForRequirement(
                    person,
                    indices,
                    slots,
                    slotIndex,
                    req
                )

                person to score
            }
            .filter { it.second > Int.MIN_VALUE }
            .sortedByDescending { it.second }
            .map { it.first }
            .applyReverseRule(req.sourceRuleId, reverseRuleIds)
            .take(req.requiredCount)

        for (person in candidates) {
            for (index in indices) {
                val i = slotIndex.byPersonIndex[person to index] ?: continue
                val slot = slots[i]

                slots[i] = slot.copy(
                    state = req.targetState,
                    taskIds = slot.taskIds + req.sourceRuleId,
                    effectiveSemantics = slot.effectiveSemantics.merge(
                        req.source.semantics
                    )
                )
            }
        }
    }

    private fun scorePersonForRequirement(
        person: Person,
        indices: List<Int>,
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        req: HouseholdRequirement
    ): Int {
        var score = 0

        for (index in indices) {
            val i = slotIndex.byPersonIndex[person to index] ?: return Int.MIN_VALUE
            val slot = slots[i]

            if (!AssignmentRules.canAssign(req, slot)) {
                return Int.MIN_VALUE
            }

            score += when (slot.state) {
                req.targetState -> 100
                SlotState.UNASSIGNED -> 50
                else -> -slot.state.weight
            }
        }

        return score
    }

    private fun List<Person>.applyReverseRule(
        sourceRuleId: String,
        reverseRuleIds: Set<String>
    ): List<Person> =
        if (sourceRuleId in reverseRuleIds)
            reversed()
        else
            this

    fun evaluateAvailability(
        requirements: List<HouseholdRequirement>,
        warningReqs: List<HouseholdRequirement>
    ): List<AvailabilityEvaluation> {

        return TimeAxis.indices.mapNotNull { index ->

            val activeReqs = requirements.filter { it.isRequiredAt(index) }

            if (activeReqs.isEmpty()) return@mapNotNull null

            val warningReqIds =
                warningReqs
                    .filter { it.isRequiredAt(index) }
                    .map{ it.sourceRuleId }

            AvailabilityEvaluation(
                index = index,
                warningReqIds = warningReqIds
            )
        }
    }

    fun generateProposals(
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        warningReq: HouseholdRequirement
    ): List<FlexResolveProposal> {

        val waiting =
            generateWaitingProposals(
                slots = slots,
                slotIndex = slotIndex,
                warningReq = warningReq
            )

        val assigned =
            generateAssignedProposals(
                slots = slots,
                slotIndex = slotIndex,
                requirements = requirements,
                warningReq = warningReq
            )

        val reverse =
            generateReverseProposals(
                slots = slots,
                slotIndex = slotIndex,
                requirements = requirements,
                warningReq = warningReq
            )

        return reverse + waiting + assigned
    }

    fun generateWaitingProposals(
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        warningReq: HouseholdRequirement
    ): List<FlexResolveProposal> {

        val isFlex =
            warningReq.flexWindowSlots.backward != 0 ||
                    warningReq.flexWindowSlots.forward != 0

        if (!isFlex) return emptyList()

        val persons = warningReq.allowedPersons
        val window = warningReq.flexWindowSlots
        val startIndex = warningReq.allIndices().first()
        val offsets = (-window.backward until 0) + (1..window.forward)

        return offsets
            .flatMap { offset ->
                val newStartIndex = startIndex + offset
                if (newStartIndex !in TimeAxis.all.indices) return@flatMap emptyList()

                val validPersons = persons.filter {
                    canAssignBlock(
                        person = it,
                        requirement = warningReq,
                        baseStartIndex = startIndex,
                        candidateStartIndex = newStartIndex,
                        slots = slots,
                        slotIndex = slotIndex
                    )
                }

                if (validPersons.size < warningReq.requiredCount) return@flatMap emptyList()

                listOf(
                    FlexResolveProposal(
                        type = ProposalType.WAITING,
                        resolvedRequirementId = warningReq.sourceRuleId,
                        sourceRuleId = warningReq.sourceRuleId,
                        requirementSource = warningReq.source,
                        requirementName = warningReq.name,
                        persons = validPersons,
                        requiredCount = warningReq.requiredCount,
                        initialIndex = startIndex,
                        candidateIndex = newStartIndex,
                        targetState = warningReq.targetState
                    )
                )
            }
            .sortedBy { it.score(slots) }
            .take(4)
    }

    fun generateAssignedProposals(
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        warningReq: HouseholdRequirement
    ): List<FlexResolveProposal> {

        val warningIndices =  warningReq.allIndices()

        val targetSlots =
            warningIndices.flatMap { index ->
                slotIndex.byIndex[index]
                    .orEmpty()
                    .map { slots[it] }
                    .filter {
                        it.person in warningReq.allowedPersons &&
                                !AssignmentRules.canAssign(warningReq, it)
                    }
            }

        val assignedReqs =
            targetSlots.flatMap { slot ->
                requirements.filter { it.sourceRuleId in slot.taskIds }
            }.distinct()

        val targetReqs =
            assignedReqs
                .filter {
                    it.flexWindowSlots.backward != 0 || it.flexWindowSlots.forward != 0
                }
                .filter {
                    it.prioritySeed >= warningReq.prioritySeed
                }
                .sortedByDescending { it.prioritySeed }

        if (targetReqs.isEmpty()) return emptyList()

        val waitingStart =  warningIndices.first()
        val waitingEnd =  warningIndices.last()

        return targetReqs
            .flatMap { targetReq ->
                val targetReqRange = targetReq.allIndices()
                val baseStartIndex = targetReqRange.first()
                val baseEndIndex = targetReqRange.last()

                // warningReq区間と重なるoffsetを除外
                val offsetPair = listOf(
                    waitingStart - baseEndIndex - 1,
                    waitingEnd - baseStartIndex + 1
                )

                val persons = targetReq.allowedPersons
                val window = targetReq.flexWindowSlots
                val offsets = (-window.backward until 0) + (1..window.forward)

                offsets
                    .flatMap { offset ->
                        if (!(offset <= offsetPair.first() || offset >= offsetPair.last())) return@flatMap emptyList()
                        val newStartIndex = baseStartIndex + offset
                        if (newStartIndex !in TimeAxis.all.indices) return@flatMap emptyList()

                        val validPersons = persons.filter {
                            canAssignBlock(
                                person = it,
                                requirement = targetReq,
                                baseStartIndex = baseStartIndex,
                                candidateStartIndex = newStartIndex,
                                slots = slots,
                                slotIndex = slotIndex
                            )
                        }

                        if (validPersons.size < targetReq.requiredCount) return@flatMap emptyList()

                        listOf(
                            FlexResolveProposal(
                                type = ProposalType.ASSIGNED,
                                resolvedRequirementId = warningReq.sourceRuleId,
                                sourceRuleId = targetReq.sourceRuleId,
                                requirementSource = targetReq.source,
                                requirementName = targetReq.name,
                                persons = validPersons,
                                requiredCount = targetReq.requiredCount,
                                initialIndex = baseStartIndex,
                                candidateIndex = newStartIndex,
                                targetState = targetReq.targetState
                            )
                        )
                    }
            }
            .sortedBy { it.score(slots) }
            .take(2)
    }

    fun generateReverseProposals(
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        warningReq: HouseholdRequirement
    ): List<FlexResolveProposal> {

        if (warningReq.requiredCount > 1 || warningReq.allowedPersons.size < 2)
            return emptyList()

        val swapList = mutableListOf<FlexResolveProposal>()

        warningReq.allowedPersons.forEach { person ->

            val targetSlots =
                warningReq.allIndices().flatMap { index ->
                    slotIndex.byIndex[index]
                        .orEmpty()
                        .map { slots[it] }
                        .filter {
                            it.person == person &&
                                    !AssignmentRules.canAssign(warningReq, it)
                        }
                }

            val assignedReqs =
                targetSlots.flatMap { slot ->
                    // taskIdsが空のslotがひとつでもあれば、personの処理全体を終了する
                    if (slot.taskIds.isEmpty()) return@forEach
                    requirements.filter { it.sourceRuleId in slot.taskIds }
                }.distinct()

            val targetReqs =
                assignedReqs
                    .filter {
                        it.requiredCount == 1 && it.allowedPersons.size == 2
                    }
                    .filter {
                        it.prioritySeed >= warningReq.prioritySeed
                    }
                    .sortedByDescending { it.prioritySeed }

            if (targetReqs.isEmpty()) return@forEach

            for (targetReq in targetReqs) {

                val reversedPerson = (targetReq.allowedPersons - person).singleOrNull()
                    ?: continue

                val targetIndices = targetReq.allIndices()
                val targetStartIndex = targetIndices.first()

                val reverseAssignable =
                    targetIndices.all { index ->
                        val slot =
                            slotIndex.byIndex[index]
                                .orEmpty()
                                .map { slots[it] }
                                .firstOrNull { it.person == reversedPerson }
                                ?: return@all false

                        AssignmentRules.canAssign(targetReq, slot)
                    }

                if (reverseAssignable)
                    swapList +=
                        FlexResolveProposal(
                            type = ProposalType.REVERSE,
                            resolvedRequirementId = warningReq.sourceRuleId,
                            sourceRuleId = targetReq.sourceRuleId,
                            requirementSource = targetReq.source,
                            requirementName = targetReq.name,
                            persons = listOf(reversedPerson),
                            requiredCount = targetReq.requiredCount,
                            initialIndex = targetStartIndex,
                            candidateIndex = targetStartIndex,
                            targetState = targetReq.targetState
                        )
            }
        }

        return swapList
    }

    private fun assignRemainingUnassignedToFree(
        slots: MutableList<TimeSlot>
    ) {
        for (i in slots.indices) {
            val slot = slots[i]

            if (slot.state == SlotState.UNASSIGNED) {
                slots[i] = slot.copy(
                    state = SlotState.FREE
                )
            }
        }
    }

    private fun mayAssignBlock(
        req: HouseholdRequirement,
        slots: List<TimeSlot>,
        slotIndex: SlotIndex
    ): Boolean {
        val indices = req.allIndices()

        val validPersons = req.allowedPersons.filter { person ->
            indices.all { index ->
                val i = slotIndex.byPersonIndex[person to index] ?: return@filter false
                AssignmentRules.canAssign(
                    req,
                    slots[i]
                )
            }
        }

        return validPersons.size >= req.requiredCount
    }

    private fun canAssignBlock(
        person: Person,
        requirement: HouseholdRequirement,
        baseStartIndex: Int,
        candidateStartIndex: Int,
        slots: List<TimeSlot>,
        slotIndex: SlotIndex
    ): Boolean {
        val indices = requirement.allIndices()
        for (i in indices) {
            val offset = i - baseStartIndex
            val targetIndex = candidateStartIndex + offset
            val slotIndexKey = person to targetIndex
            val idx = slotIndex.byPersonIndex[slotIndexKey] ?: return false
            val slot = slots[idx]

            if (!canResolveToSlot(
                    requirement,
                    slot
                )
            ) {
                return false
            }
        }
        return true
    }

    fun canResolveToSlot(
        req: HouseholdRequirement,
        slot: TimeSlot
    ): Boolean {
        return slot.state.weight <= req.targetState.weight
    }
}
