package com.example.familyscheduler.domain.interaction

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.RequirementSemantics
import com.example.familyscheduler.domain.requirement.RequirementToggleOverride
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis

class TimelineBlockBuilder {

    fun build(
        rules: List<HouseholdRequirementRule>,
        requirements: List<HouseholdRequirement>,
        slotsByIndex: Map<Int, List<TimeSlot>>,
        requirementOverrides: List<RequirementOverride>
    ): List<TimelineBlock> {

        val items =
            rules.buildItems(
                requirements = requirements,
                slotsByIndex = slotsByIndex,
                requirementOverrides = requirementOverrides
            )

        return mergeAdjacent(items)
    }

    private fun List<HouseholdRequirementRule>.buildItems(
        requirements: List<HouseholdRequirement>,
        slotsByIndex: Map<Int, List<TimeSlot>>,
        requirementOverrides: List<RequirementOverride>
    ): List<TimelineBlock> {

        return mapNotNull { rule ->
            val mode =
                resolveMode(
                    id = rule.id,
                    requirementOverrides = requirementOverrides
                )

            val req = requirements
                .filterIsInstance<TimeRangeHouseholdRequirement>()
                .find { it.sourceRuleId == rule.id }

            if (req == null) {
                TimelineBlock(
                    startIndex = TimeAxis.indexOf(rule.timeRange.start),
                    endIndex = TimeAxis.indexOf(rule.timeRange.end),
                    semantics = rule.source.semantics,
                    mode = mode,
                    assignedPersons = emptyList(),
                    assignablePersons = emptyList(),
                    blockingPersons = emptyList(),
                    requiredCount = 0,
                    requirementIds = listOf(rule.id),
                    allowedActions = setOf(BlockAction.EDIT, BlockAction.CANCEL)
                )
            } else {

                val assignedPersons =
                    findAssignedPersons(
                        req = req,
                        slotsByIndex = slotsByIndex
                    )

                val assignablePersons =
                    findAssignablePersons(
                        req = req,
                        slotsByIndex = slotsByIndex
                    )

                val blockingPersons =
                    req.allowedPersons - assignablePersons.toSet()

                val requiredCount = req.requiredCount

                val semantics =
                    if (assignablePersons.size < requiredCount) {
                        req.source.semantics
                    } else {
                        resolveSemantics(
                            req = req,
                            slotsByIndex = slotsByIndex
                        )
                    }

                if (semantics == null) return@mapNotNull null

                TimelineBlock(
                    startIndex = req.startIndex,
                    endIndex = req.endIndex,
                    semantics = semantics,
                    mode = mode,
                    assignedPersons = assignedPersons,
                    assignablePersons = assignablePersons,
                    blockingPersons = blockingPersons,
                    requiredCount = requiredCount,
                    requirementIds = listOf(req.sourceRuleId),
                    allowedActions =
                        resolveAllowedActions(
                            rule = rule,
                            mode = mode,
                            assignablePersons = assignablePersons,
                            req = req,
                            blockingPersons = blockingPersons,
                            slotsByIndex = slotsByIndex
                        )
                )
            }
        }
    }

    private fun resolveMode(
        id: String,
        requirementOverrides: List<RequirementOverride>
    ): RequirementModeToday {

        requirementOverrides
            .filterIsInstance<RequirementToggleOverride>()
            .firstOrNull { it.ruleId == id }
            ?.let {
                return it.mode
            }

        return RequirementModeToday.AUTO
    }

    private fun findAssignedPersons(
        req: TimeRangeHouseholdRequirement,
        slotsByIndex: Map<Int, List<TimeSlot>>
    ): List<Person> {

        return req.allowedPersons.filter { person ->

            (req.startIndex until req.endIndex).all { index ->

                slotsByIndex[index]
                    .orEmpty()
                    .any { slot ->

                        slot.person == person &&
                                slot.state == req.targetState &&
                                req.sourceRuleId in slot.taskIds
                    }
            }
        }
    }

    private fun findAssignablePersons(
        req: TimeRangeHouseholdRequirement,
        slotsByIndex: Map<Int, List<TimeSlot>>
    ): List<Person> {

        return req.allowedPersons.filter { person ->

            canAssignToBlock(
                req = req,
                person = person,
                slotsByIndex = slotsByIndex
            )
        }
    }

    private fun canAssignToBlock(
        req: TimeRangeHouseholdRequirement,
        person: Person,
        slotsByIndex: Map<Int, List<TimeSlot>>
    ): Boolean {

        return (req.startIndex until req.endIndex).all { index ->

            val slot =
                slotsByIndex[index]
                    .orEmpty()
                    .firstOrNull {
                        it.person == person
                    }
                    ?: return@all false

            when {
                slot.state.weight > req.targetState.weight -> {
                    false
                }
                slot.state.weight < req.targetState.weight -> {
                    true
                }
                else -> {
                    req.source.semantics.canCoexist(slot.effectiveSemantics)
                }
            }
        }
    }

    private fun resolveAllowedActions(
        rule: HouseholdRequirementRule,
        mode: RequirementModeToday,
        assignablePersons: List<Person>,
        req: TimeRangeHouseholdRequirement,
        blockingPersons: List<Person>,
        slotsByIndex: Map<Int, List<TimeSlot>>
    ): Set<BlockAction> {

        val actions = mutableSetOf<BlockAction>()

        val hasSoloAlternative =
            assignablePersons.size == 1 && rule.requiredCount == 2

        val hasReverseAlternative =
            assignablePersons.size > rule.requiredCount ||
                    // 以下、挙動を検証中
                    // StateTextPresentationのUI表示も同時に検証中
                    canReverseToAvoidDeadlock(
                        rule = rule,
                        req = req,
                        assignablePersons = assignablePersons,
                        blockingPersons = blockingPersons,
                        slotsByIndex = slotsByIndex
                    )

        if (rule.source.semantics == RequirementSemantics.TASK) {
            actions += BlockAction.EDIT
            actions += BlockAction.CANCEL
        }

        if (
            hasSoloAlternative &&
            mode != RequirementModeToday.REVERSE
        ) {
            actions += BlockAction.SOLO
        }

        if (
            hasReverseAlternative &&
            mode != RequirementModeToday.SOLO
        ) {
            actions += BlockAction.REVERSE
        }

        return actions
    }

    private fun canReverseToAvoidDeadlock(
        rule: HouseholdRequirementRule,
        req: TimeRangeHouseholdRequirement,
        assignablePersons: List<Person>,
        blockingPersons: List<Person>,
        slotsByIndex: Map<Int, List<TimeSlot>>
    ): Boolean {

        if (
            rule.targetState != SlotState.CHILDCARE ||
            rule.source.semantics != RequirementSemantics.TASK ||
            rule.requiredCount != 1 ||
            assignablePersons.size != 1 ||
            blockingPersons.size != 1
        ) {
            return false
        }

        val blockingPerson = blockingPersons.single()

        return (req.startIndex until req.endIndex).all { index ->

            val slot =
                slotsByIndex[index]
                    .orEmpty()
                    .firstOrNull {
                        it.person == blockingPerson
                    }
                    ?: return@all false

            slot.state.weight <= req.targetState.weight
        }
    }

    private fun resolveSemantics(
        req: TimeRangeHouseholdRequirement,
        slotsByIndex: Map<Int, List<TimeSlot>>
    ): RequirementSemantics? {

        val effectiveSemanticsMap =
            (req.startIndex until req.endIndex)
                .flatMap { index ->
                    slotsByIndex[index].orEmpty()
                }
                .filter { slot ->
                    req.sourceRuleId in slot.taskIds
                }
                .map { it.effectiveSemantics }

        return if (req.source.semantics in effectiveSemanticsMap) {
            req.source.semantics
        } else {
            null
        }
    }

    private fun mergeAdjacent(
        items: List<TimelineBlock>
    ): List<TimelineBlock> {

        if (items.isEmpty()) return emptyList()

        val sorted =
            items.sortedBy { it.startIndex }

        val merged =
            mutableListOf<MutableList<TimelineBlock>>()

        var current =
            mutableListOf(sorted.first())

        for (item in sorted.drop(1)) {

            val prev = current.last()

            val mergeable =
                prev.endIndex == item.startIndex &&
                        prev.mode == item.mode &&
                        prev.assignedPersons.toSet() == item.assignedPersons.toSet() &&
                        prev.assignablePersons.toSet() == item.assignablePersons.toSet() &&
                        prev.blockingPersons.toSet() == item.blockingPersons.toSet() &&
                        prev.requiredCount == item.requiredCount &&
                        prev.allowedActions == item.allowedActions &&
                        prev.semantics == item.semantics

            if (mergeable) {
                current += item
            } else {
                merged += current
                current = mutableListOf(item)
            }
        }

        merged += current

        return merged.map { group ->

            TimelineBlock(
                startIndex = group.first().startIndex,
                endIndex = group.last().endIndex,
                semantics = group.first().semantics,
                mode = group.first().mode,
                assignedPersons = group.first().assignedPersons,
                assignablePersons = group.first().assignablePersons,
                blockingPersons = group.first().blockingPersons,
                requiredCount = group.first().requiredCount,
                requirementIds = group.flatMap { it.requirementIds },
                allowedActions = group.first().allowedActions
            )
        }
    }
}