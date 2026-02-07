package com.example.familyscheduler.domain.template

import java.time.DayOfWeek
import java.time.LocalDate

object RepeatRuleEvaluator {

    fun appliesToday(rule: RepeatRule, date: LocalDate): Boolean {
        return when (rule) {
            RepeatRule.DAILY -> true
            RepeatRule.WEEKDAY_ONLY ->
                date.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

            RepeatRule.WEEKEND_ONLY ->
                date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

            RepeatRule.WEEKLY_1 -> true        // 仮
            RepeatRule.WEEKLY_3 -> true        // 仮
        }
    }
}
