package com.example.familyscheduler.domain.schedule

import java.time.DayOfWeek
import java.time.LocalDate

sealed class RepeatRule {


    object None : RepeatRule()  // 追加
    object Daily : RepeatRule()

    data class Weekly(
        val days: Set<DayOfWeek>
    ) : RepeatRule()

    fun appliesTo(date: LocalDate): Boolean =
        when (this) {
            None -> false   //　追加
            Daily -> true
            is Weekly -> date.dayOfWeek in days
        }
}
