package com.example.familyscheduler.domain.routine

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

class ChildCareRuleConverter(
    private val capacityCalculator: CareCapacityCalculator,
    private val allowedPersons: Set<Person>,
) {

    fun convert(
        blocks: List<ChildCareBlock>,
        date: LocalDate
    ): List<HouseholdRequirementRule> {

        return blocks
            .filter { it.activeChildrenCount > 0 }
            .map { toRule(it, date) }
    }

    private fun toRule(
        block: ChildCareBlock,
        date: LocalDate
    ): HouseholdRequirementRule {

        val requiredCount =
            capacityCalculator.calculateRequiredCount(
                block.activeChildrenCount
            )

        val id = householdRequirementRuleKey(
            date = date,
            source = RequirementSource.CHILD_ROUTINE.toString(),
            taskName = block.label.toString(),
            timeRange = TimeRange(
                start = block.startTime,
                end = block.endTime
            )
        )

        return HouseholdRequirementRule(
            id = id,
            source = RequirementSource.CHILD_ROUTINE,
            taskName = taskNameOf(block.label),
            targetState = SlotState.CHILDCARE, // 固定
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

    private fun householdRequirementRuleKey(
        date: LocalDate,
        source: String,
        taskName: String,
        timeRange: TimeRange
    ): String {
        return buildString {
            append(date)
            append("_")
            append(source)
            append("_")
            append(taskName)
            append("_")
            append(timeRange.start)
            append("_")
            append(timeRange.end)
        }
    }

    private fun taskNameOf(
        label: ChildCareLabel?
    ): String =
        when (label) {
            ChildCareLabel.NURSERY_DROP_OFF -> "登園"
            ChildCareLabel.NURSERY_PICKUP -> "お迎え"
            null -> ""
        }

    private fun toFlexWindow(
        block: ChildCareBlock
    ): FlexWindowParameters {

        val earliest = block.flexEarliest
        val latest = block.flexLatest

        if (earliest == null || latest == null) {
            return FlexWindowParameters(0, 0)
        }

        val backwardMinutes =
            Duration.between(earliest, block.startTime)
                .toMinutes()
                .coerceAtLeast(0)

        val forwardMinutes =
            Duration.between(block.startTime, latest)
                .toMinutes()
                .coerceAtLeast(0)

        val backwardSlots = (backwardMinutes / TimeAxis.stepMinutes).toInt()
        val forwardSlots = (forwardMinutes / TimeAxis.stepMinutes).toInt()

        return FlexWindowParameters(
            backward = backwardSlots,
            forward = forwardSlots
        )
    }
}