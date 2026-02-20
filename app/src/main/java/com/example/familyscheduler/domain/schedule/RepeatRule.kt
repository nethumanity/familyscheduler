package com.example.familyscheduler.domain.schedule

import java.time.DayOfWeek
import java.time.LocalDate

sealed class RepeatRule {   //DailyTemplate→DailyState生成時に、WeeklyがDailyに対して優先される

    object Daily : RepeatRule()

    data class Weekly(
        val days: Set<DayOfWeek>
    ) : RepeatRule()

    fun appliesTo(date: LocalDate): Boolean =
        when (this) {
            Daily -> true
            is Weekly -> date.dayOfWeek in days
        }
}
