package com.example.familyscheduler.domain.routine

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import com.example.familyscheduler.ui.state.SettingsUiState

class ChildCareRuleConverter(
    private val capacityCalculator: CareCapacityCalculator,
    private val allowedPersons: List<Person>,
) {

    fun convert(
        blocks: List<ChildCareBlock>,
        settings: SettingsUiState
    ): List<HouseholdRequirementRule> {

        return blocks
            .filter { it.activeChildrenCount > 0 }
            .map { toRule(it, settings) }
    }

    private fun toRule(
        block: ChildCareBlock,
        settings: SettingsUiState
    ): HouseholdRequirementRule {

        val requiredCount =
            capacityCalculator.calculateRequiredCount(
                activeChildrenCount = block.activeChildrenCount,
                maxChildrenPerAdult = settings.maxChildrenPerAdult
            )

        return HouseholdRequirementRule(
            id = block.id,
            source = block.label?.source
                ?: RequirementSource.CHILD_ROUTINE,
            taskName = block.label?.taskName.orEmpty(),
            targetState = SlotState.CHILDCARE,
            requiredCount = requiredCount,
            allowedPersons = allowedPersons,
            flexWindowSlots = toFlexWindow(block),
            date = block.date,
            daysOfWeek = null,
            timeRange = TimeRange(
                start = block.startTime,
                end = block.endTime
            )
        )
    }

    private fun toFlexWindow(
        block: ChildCareBlock
    ): FlexWindowParameters {

        val earliest = block.flexEarliest
        val latest = block.flexLatest

        if (earliest == null || latest == null) {
            return FlexWindowParameters(0, 0)
        }

        val backward =
            TimeAxis.distance(
                from = earliest,
                to = block.startTime
            ).coerceAtLeast(0)

        val forward =
            TimeAxis.distance(
                from = block.startTime,
                to = latest
            ).coerceAtLeast(0)

        return FlexWindowParameters(
            backward = backward,
            forward = forward
        )
    }
}