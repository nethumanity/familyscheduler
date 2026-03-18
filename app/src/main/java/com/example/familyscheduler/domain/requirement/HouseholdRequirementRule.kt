package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

data class HouseholdRequirementRule(
    val id: String = UUID.randomUUID().toString(),
    val source: RequirementSource = RequirementSource.USER,
    val taskName: String,
    val targetState: SlotState,
    val requiredCount: Int,
    val allowedPersons: Set<Person>,
    val flexWindowSlots: FlexWindowParameters,
    val date: LocalDate?,               // 入力系①日付指定パターン用
    val daysOfWeek: Set<DayOfWeek>?,    // 入力系②毎日/曜日指定パターン、③子どもルーティン用
    val timeRange: TimeRange
) {
    fun isActiveOn(date: LocalDate): Boolean {

        if (this.date != null) {
            return this.date == date
        }

        if (daysOfWeek != null) {
            return date.dayOfWeek in daysOfWeek
        }

        return false
    }

    fun toRequirement(): TimeRangeHouseholdRequirement {

        val startIndex = TimeAxis.indexOf(timeRange.start)
        val endIndex = TimeAxis.indexOf(timeRange.end)

        return TimeRangeHouseholdRequirement(
            name = taskName,
            targetState = targetState,
            requiredCount = requiredCount,
            allowedPersons = allowedPersons,
            flexWindowSlots = flexWindowSlots,
            startIndex = startIndex,
            endIndex = endIndex
        )
    }
}