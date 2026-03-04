package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeRange
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime

data class ChildCareBlock(
    val daysOfWeek: Set<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val label: ChildCareLabel? = null,
    val flexEarliest: LocalTime? = null,
    val flexLatest: LocalTime? = null,
    val activeChildrenCount: Int
) {

    fun convertToRules(
        blocks: List<ChildCareBlock>,
        allowedPersons: Set<Person>,
        capacityCalculator: CareCapacityCalculator,
        stepMinutes: Int
    ): List<HouseholdRequirementRule> {

        return blocks
            .filter { it.activeChildrenCount > 0 }
            .map { block ->

                val requiredCount =
                    capacityCalculator.calculateRequiredCount(
                        block.activeChildrenCount
                    )

                HouseholdRequirementRule(
                    taskName = when (block.label) {
                        ChildCareLabel.NURSERY_DROP_OFF -> "登園"
                        ChildCareLabel.NURSERY_PICKUP -> "お迎え"
                        null -> ""
                    },
                    targetState = SlotState.CHILDCARE, // 固定
                    requiredCount = requiredCount,
                    allowedPersons = allowedPersons,
                    flexWindowSlots = toFlexWindow(block, stepMinutes),
                    date = null,
                    daysOfWeek = block.daysOfWeek,
                    timeRange = TimeRange(
                        start = block.startTime,
                        end = block.endTime
                    )
                )
            }
    }

    private fun toFlexWindow(
        block: ChildCareBlock,
        stepMinutes: Int
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
}