package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.RequirementToggleOverride
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

        val evaluations =
            evaluateAvailability(
                slots = workingSlots,
                slotIndex = index,
                requirements = requirements
            )

        assignRemainingUnassignedToFree(workingSlots)

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
        //if (requirements.isNullOrEmpty()) return
        val orderedReqs = requirements.sortedByDescending {
            it.prioritySeed
        }
        val reverseRuleIds = overrides
            .filterIsInstance<RequirementToggleOverride>()
            .filter { it.mode == RequirementModeToday.REVERSE }
            .map { it.ruleId }
            .toSet()
        for (req in orderedReqs) {
            if (!mayAssignBlock(req, slots, slotIndex, reverseRuleIds)) continue
            assignBlock(req, slots, slotIndex, reverseRuleIds)
        }
    }

    private fun mayAssignBlock(
        req: HouseholdRequirement,
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        reverseRuleIds: Set<String>
    ): Boolean {
        val indices = req.allIndices()
        val orderedPersons = req.orderedPersons(reverseRuleIds)

        val validPersons = orderedPersons.filter { person ->
            indices.all { index ->
                val i = slotIndex.byPersonIndex[person to index] ?: return@filter false
                val slot = slots[i]
                slot.state.weight <= req.targetState.weight
                //slot.state == SlotState.UNASSIGNED ||
                //        slot.state == req.targetState
            }
        }

        return validPersons.size >= req.requiredCount
    }

    private fun assignBlock(
        req: HouseholdRequirement,
        slots: MutableList<TimeSlot>,
        slotIndex: SlotIndex,
        reverseRuleIds: Set<String>
    ) {
        val indices = req.allIndices()
        val orderedPersons = req.orderedPersons(reverseRuleIds)

        val candidates = orderedPersons
            .mapNotNull { person ->

                val isValid = indices.all { index ->
                    val i = slotIndex.byPersonIndex[person to index] ?: return@mapNotNull null
                    val slot = slots[i]
                    slot.state.weight <= req.targetState.weight
                }

                if (!isValid) return@mapNotNull null

                val score = scorePersonForRequirement(
                    person,
                    indices,
                    slots,
                    slotIndex,
                    req.targetState
                )

                person to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(req.requiredCount)

        for (person in candidates) {
            for (index in indices) {
                val i = slotIndex.byPersonIndex[person to index] ?: continue
                val slot = slots[i]

                slots[i] = slot.copy(
                    state = req.targetState,
                    taskIds = slot.taskIds + req.sourceRuleId
                )
            }
        }
    }

    private fun HouseholdRequirement.orderedPersons(
        reverseRuleIds: Set<String>
    ): List<Person> =
        if (sourceRuleId in reverseRuleIds)
            allowedPersons.reversed()
        else
            allowedPersons

    private fun scorePersonForRequirement(
        person: Person,
        indices: List<Int>,
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        targetState: SlotState
    ): Int {

        var score = 0

        for (index in indices) {
            val i = slotIndex.byPersonIndex[person to index] ?: return Int.MIN_VALUE
            val slot = slots[i]

            score += when (slot.state) {
                targetState -> 100
                SlotState.UNASSIGNED -> 50
                else -> -slot.state.weight
            }
        }

        return score
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

    fun evaluateAvailability(
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>
    ): List<AvailabilityEvaluation> {

        return TimeAxis.indices.mapNotNull { index ->

            val slotsAtIndex = slotIndex.byIndex[index].orEmpty().map { slots[it] }

            val activeReqs = requirements.filter { it.isRequiredAt(index) }

            if (activeReqs.isEmpty()) return@mapNotNull null

            val reasons = mutableListOf<ReasonEvaluation>()

            activeReqs.forEach { req ->

                val satisfiedPersons =
                    slotsAtIndex
                        .filter { slot ->
                            slot.person in req.allowedPersons &&
                                    slot.state == req.targetState
                        }
                        .map { it.person }

                val requiredCount = req.requiredCount
                val assignedCount = satisfiedPersons.size
                val missing = requiredCount - assignedCount

                if (missing > 0) {
                    val info =
                        collectBlockInfo(
                            slots = slotsAtIndex,
                            persons = req.allowedPersons.toList() - satisfiedPersons
                        )
                    val reason =
                        MissingReason(
                            sourceRuleId = req.sourceRuleId,
                            requirementName = req.name,
                            requiredCount = requiredCount,
                            assignedCount = assignedCount,
                            blockingPersons = info
                        )

                    val waiting =
                        generateWaitingProposals(
                            slots = slots,
                            slotIndex = slotIndex,
                            requirements = activeReqs,
                            reason = reason
                        )
                    val assigned =
                        generateAssignedProposals(
                            index = index,
                            slots = slots,
                            slotIndex = slotIndex,
                            requirements = activeReqs,
                            reason = reason
                        )
                    reasons.add(
                        ReasonEvaluation(
                            reason = reason,
                            proposals = (waiting + assigned)
                        )
                    )
                }
            }

            AvailabilityEvaluation(
                index = index,
                hasFlexRequirement = activeReqs.any { it.flexWindowSlots.backward != 0 || it.flexWindowSlots.forward != 0 },
                missing = reasons.size,
                reasons = reasons,
            )
        }
    }

    fun collectBlockInfo(
        slots: List<TimeSlot>,
        persons: List<Person>,
    ): BlockInfo {

        val slotByPerson = slots.filter { it.person in persons }

        return BlockInfo(
                person = persons,
                currentState = slotByPerson.map { it.state },
                taskIds = slotByPerson.flatMap { it.taskIds }
        )
    }

    fun generateWaitingProposals(
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        reason: MissingReason
    ): List<FlexResolveProposal> {

        val hasFlexRequirement =
            requirements.any {
                it.flexWindowSlots.backward != 0 ||
                        it.flexWindowSlots.forward != 0
            }

        if (!hasFlexRequirement) return emptyList()

        return generateFlexResolveProposalsForReason(
            slots = slots,
            slotIndex = slotIndex,
            requirements = requirements,
            reason = reason
        )
            .sortedBy { it.score(slots) }
            .take(4)
    }

    fun generateFlexResolveProposalsForReason(
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        reason: MissingReason
    ): List<FlexResolveProposal> {

        val requirement = requirements
            .find { it.sourceRuleId == reason.sourceRuleId }
            ?: return emptyList()

        val persons = requirement.allowedPersons.toList()
        val window = requirement.flexWindowSlots
        val offsets = (-window.backward until 0) + (1..window.forward)

        return offsets.flatMap { offset ->

            val startIndex = requirement.allIndices().first()
            val newStartIndex = startIndex + offset
            if (newStartIndex !in TimeAxis.all.indices) return@flatMap emptyList()

            val validPersons = persons.filter {
                canAssignBlock(
                    person = it,
                    requirement = requirement,
                    baseStartIndex = startIndex,
                    candidateStartIndex = newStartIndex,
                    slots = slots,
                    slotIndex = slotIndex
                )
            }

            when {
                // 1人必要 → 全員分 proposal
                requirement.requiredCount == 1 -> {
                    validPersons.map { person ->
                        FlexResolveProposal(
                            type = ProposalType.WAITING,
                            sourceRuleId = requirement.sourceRuleId,
                            requirementSource = requirement.source,
                            requirementName = reason.requirementName,
                            persons = listOf(person),
                            initialIndex = startIndex,
                            candidateIndex = newStartIndex,
                            targetState = requirement.targetState
                        )
                    }
                }

                // 複数人必要 → まとめて1 proposal
                validPersons.size >= requirement.requiredCount -> {
                    listOf(
                        FlexResolveProposal(
                            type = ProposalType.WAITING,
                            sourceRuleId = requirement.sourceRuleId,
                            requirementSource = requirement.source,
                            requirementName = reason.requirementName,
                            persons = validPersons.take(requirement.requiredCount),
                            initialIndex = startIndex,
                            candidateIndex = newStartIndex,
                            targetState = requirement.targetState
                        )
                    )
                }

                else -> emptyList()
            }
        }
    }

    fun canAssignBlock(
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

            if (slot.state.weight > requirement.targetState.weight) {
                return false
            }
        }
        return true
    }

    fun generateAssignedProposals(
        index: Int,
        slots: List<TimeSlot>,
        slotIndex: SlotIndex,
        requirements: List<HouseholdRequirement>,
        reason: MissingReason
    ): List<FlexResolveProposal> {

        val hasFlexRequirement =
            requirements.any {
                it.flexWindowSlots.backward != 0 ||
                        it.flexWindowSlots.forward != 0
            }

        if (!hasFlexRequirement) return emptyList()

        val slotsAtIndex = slotIndex.byIndex[index].orEmpty().map { slots[it] }
        val waitingReq = requirements.find { it.sourceRuleId == reason.sourceRuleId }
            ?: return emptyList()

        val waitingStart =  waitingReq.allIndices().first()
        val waitingEnd =  waitingReq.allIndices().last()

        return slotsAtIndex
            .filter { it.person in waitingReq.allowedPersons &&
                    it.state != waitingReq.targetState }
            .flatMap { slot ->
                val assignedReqs =
                    requirements.filter { it.sourceRuleId in slot.taskIds }
                val targetReq = assignedReqs.maxByOrNull { it.prioritySeed }
                    ?: return@flatMap emptyList()

                val targetReqRange = targetReq.allIndices()
                val baseStartIndex = targetReqRange.first()
                val baseEndIndex = targetReqRange.last()
                val offsetPair = listOf(
                    waitingStart - baseEndIndex,
                    waitingEnd - baseStartIndex
                    )
                val persons = targetReq.allowedPersons.toList()
                val window = targetReq.flexWindowSlots
                val offsets = (-window.backward until 0) + (1..window.forward)

                offsets.flatMap { offset ->
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

                    when {
                        // 1人必要 → 全員分 proposal
                        targetReq.requiredCount == 1 -> {
                            validPersons.map { person ->
                                FlexResolveProposal(
                                    type = ProposalType.ASSIGNED,
                                    sourceRuleId = targetReq.sourceRuleId,
                                    requirementSource = targetReq.source,
                                    requirementName = targetReq.name,
                                    persons = listOf(person),
                                    initialIndex = baseStartIndex,
                                    candidateIndex = newStartIndex,
                                    targetState = targetReq.targetState
                                )
                            }
                        }
                        // 複数人必要 → まとめて1 proposal
                        validPersons.size >= targetReq.requiredCount -> {
                            listOf(
                                FlexResolveProposal(
                                    type = ProposalType.ASSIGNED,
                                    sourceRuleId = targetReq.sourceRuleId,
                                    requirementSource = targetReq.source,
                                    requirementName = targetReq.name,
                                    persons = validPersons.take(targetReq.requiredCount),
                                    initialIndex = baseStartIndex,
                                    candidateIndex = newStartIndex,
                                    targetState = targetReq.targetState
                                )
                            )
                        }
                        else -> emptyList()
                    }
                }
            }
            .distinctBy { Triple(it.sourceRuleId, it.candidateIndex, it.persons.sorted()) }
            .sortedBy { it.score(slots) }
            .take(2)
        }
    }
