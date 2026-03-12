package com.example.familyscheduler.domain.routine

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import java.time.Duration

class ChildCareRuleConverter(
    private val capacityCalculator: CareCapacityCalculator,
    private val allowedPersons: Set<Person>,
) {

    fun convert(
        blocks: List<ChildCareBlock>
    ): List<HouseholdRequirementRule> {

        return blocks
            .filter { it.activeChildrenCount > 0 }
            .map { toRule(it) }
    }

    private fun toRule(
        block: ChildCareBlock
    ): HouseholdRequirementRule {

        val requiredCount =
            capacityCalculator.calculateRequiredCount(
                block.activeChildrenCount
            )

        return HouseholdRequirementRule(
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

    /* 旧バージョン
    private fun toFlexWindow(
        block: ChildCareBlock,
        //stepMinutes: Int
    ): FlexWindowParameters {

        if (block.flexEarliest == null || block.flexLatest == null) {
            return FlexWindowParameters(0, 0)
        }

        val backward =
            Duration.between(block.flexEarliest, block.startTime)
                .toMinutes()
                .div(stepMinutes)
                .toInt()

        val forward =
            Duration.between(block.startTime, block.flexLatest)
                .toMinutes()
                .div(stepMinutes)
                .toInt()

        return FlexWindowParameters(
            backward = backward.coerceAtLeast(0),
            forward = forward.coerceAtLeast(0)
        )
    }
     */
}