package com.example.familyscheduler.domain.schedule

import java.time.DayOfWeek
import java.time.LocalDate

sealed class RepeatRule {


    object None : RepeatRule()
    object Daily : RepeatRule()

    data class Weekly(
        val days: Set<DayOfWeek>
    ) : RepeatRule()

    fun appliesTo(date: LocalDate): Boolean =
        when (this) {
            None -> false
            Daily -> true
            is Weekly -> date.dayOfWeek in days
        }

    fun specificity(): Int = when (this) {
        None -> 0
        Daily -> 1
        is Weekly -> 10 + (7 - this.days.size)
    }
}
