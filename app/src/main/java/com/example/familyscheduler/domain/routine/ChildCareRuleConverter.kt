package com.example.familyscheduler.domain.routine

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import com.example.familyscheduler.ui.utilities.SettingsUiState
import java.time.Duration

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
            id = block.eventId,
            source = sourceOf(block.label),
            taskName = taskNameOf(block.label),
            targetState = SlotState.CHILDCARE,
            requiredCount = requiredCount,
            allowedPersons = allowedPersons,
            flexWindowSlots = toFlexWindow(block),
            date = null,
            daysOfWeek = block.daysOfWeek,
            timeRange = TimeRange(
                start = block.startTime,
                end = block.endTime
            )
        )
    }

    private fun taskNameOf(
        label: ChildCareLabel?
    ): String =
        when (label) {
            ChildCareLabel.NURSERY_DROP_OFF -> "登園"
            ChildCareLabel.NURSERY_PICKUP -> "お迎え"
            null -> ""
        }

    private fun sourceOf(
        label: ChildCareLabel?
    ): RequirementSource =
        when (label) {
            ChildCareLabel.NURSERY_DROP_OFF -> RequirementSource.NURSERY_DROP_OFF
            ChildCareLabel.NURSERY_PICKUP -> RequirementSource.NURSERY_PICKUP
            else -> RequirementSource.CHILD_ROUTINE
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
            (TimeAxis.indexOf(block.startTime) - TimeAxis.indexOf(earliest))
                .coerceAtLeast(0)
        val forward =
            (TimeAxis.indexOf(latest) - TimeAxis.indexOf(block.startTime))
                .coerceAtLeast(0)

        return FlexWindowParameters(
            backward = backward,
            forward = forward
        )
    }
}