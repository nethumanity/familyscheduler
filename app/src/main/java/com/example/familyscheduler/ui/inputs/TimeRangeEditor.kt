package com.example.familyscheduler.ui.inputs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.ScheduleType
import com.example.familyscheduler.domain.schedule.StateCategory
import com.example.familyscheduler.domain.time.TimeRange
import com.example.familyscheduler.domain.time.TimeDropdownPicker
import java.time.LocalTime

@Composable
fun TimeRangeEditor(
    category: StateCategory,
    title: String, // ← 追加：何のための時間枠か識別するため
    schedules: MutableList<ScheduleTemplate>
) {

    var start by remember {
        mutableStateOf(LocalTime.of(9, 0))
    }

    var end by remember {
        mutableStateOf(LocalTime.of(17, 0))
    }

    fun updateScheduleIfValid() {

        val type =
            ScheduleType(
                title = title,
                category = category
            )

        schedules.removeAll {
            //it.type.category == category
            // カテゴリではなく「タイトル」で重複排除する（通勤の重複を防ぐ）
            it.type.title == title
        }

        if (start == end) return

        schedules.add(
            ScheduleTemplate(
                type = type,
                timeRange = TimeRange(start = start, end = end)
            )
        )
    }

    LaunchedEffect(start, end) {
        updateScheduleIfValid()
    }

    TimeDropdownPicker(
        label = "開始",
        selectedTime = start
    ) {
        start = it
    }

    TimeDropdownPicker(
        label = "終了",
        selectedTime = end
    ) {
        end = it
    }
}
