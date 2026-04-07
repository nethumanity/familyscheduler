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
    val timeRange: TimeRange,
    val createdAt: Long = System.currentTimeMillis()
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
            sourceRuleId = id,
            name = taskName,
            targetState = targetState,
            requiredCount = requiredCount,
            allowedPersons = allowedPersons,
            flexWindowSlots = flexWindowSlots,
            startIndex = startIndex,
            endIndex = endIndex,
            prioritySeed = this.prioritySeed(startIndex, endIndex)
        )
    }

    // 最上位（or 次点）のpriorityにrequiredCount, allowedPersonsの組合せを入れるべきかもしれない
    // Count = 2, PersonsSize = 2, Count = 1, PersonsSize = 1 > Count = 1, PersonsSize = 2
    fun prioritySeed(startIndex: Int, endIndex: Int): Long {
        val length = endIndex - startIndex
        val flex = flexWindowSlots.backward + flexWindowSlots.forward

        return targetState.weight * 1_000_000L +                         // WORK > CHILDCARE > LIFE
                (if (date != null) 100_000 else 0) +
                (if (source == RequirementSource.USER) 10_000 else 0) +  // あまり意味はない
                ((1000 - length) * 100L) +                               // 逆がいい気もするが、試しながら考える
                ((100 - flex) * 10L) +
                createdAt
    }
}