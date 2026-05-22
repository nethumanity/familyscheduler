package com.example.familyscheduler.ui.presentation

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek

object DayOfWeekPresentation {

    fun short(day: DayOfWeek): String =
        when (day) {
            DayOfWeek.MONDAY -> "月"
            DayOfWeek.TUESDAY -> "火"
            DayOfWeek.WEDNESDAY -> "水"
            DayOfWeek.THURSDAY -> "木"
            DayOfWeek.FRIDAY -> "金"
            DayOfWeek.SATURDAY -> "土"
            DayOfWeek.SUNDAY -> "日"
        }

    fun long(day: DayOfWeek): String =
        when (day) {
            DayOfWeek.MONDAY -> "月曜日"
            DayOfWeek.TUESDAY -> "火曜日"
            DayOfWeek.WEDNESDAY -> "水曜日"
            DayOfWeek.THURSDAY -> "木曜日"
            DayOfWeek.FRIDAY -> "金曜日"
            DayOfWeek.SATURDAY -> "土曜日"
            DayOfWeek.SUNDAY -> "日曜日"
        }

    fun color(
        day: DayOfWeek,
        defaultColor: Color
    ): Color =
        when(day) {
            DayOfWeek.SATURDAY -> Color.Companion.Blue
            DayOfWeek.SUNDAY -> Color.Companion.Red
            else -> defaultColor
        }
}