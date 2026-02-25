package com.example.familyscheduler.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.ScheduleType
import com.example.familyscheduler.domain.schedule.StateCategory
import com.example.familyscheduler.domain.schedule.TimeRange.Companion.createOrNull
import com.example.familyscheduler.domain.time.TimeDropdownPicker
import java.time.LocalTime

@Composable
fun TimeRangeEditor(
    person: Person,
    category: StateCategory,
    schedules: MutableList<ScheduleTemplate>
) {

    var start by remember {
        mutableStateOf(LocalTime.of(9, 0))
    }

    var end by remember {
        mutableStateOf(LocalTime.of(17, 0))
    }

    fun updateScheduleIfValid() {

        val timeRange =
            createOrNull(start, end)
                ?: return   // ← 無効なら何もしない（重要）

        val type =
            ScheduleType(
                title = category.name,
                category = category
            )

        schedules.removeAll {
            it.type.category == category
        }

        schedules.add(
            ScheduleTemplate(
                person = person,
                type = type,
                timeRange = timeRange,
                repeatRule = RepeatRule.Daily
            )
        )
    }

    TimeDropdownPicker(
        label = "開始",
        selectedTime = start
    ) {
        start = it
        updateScheduleIfValid()
    }

    TimeDropdownPicker(
        label = "終了",
        selectedTime = end
    ) {
        end = it
        updateScheduleIfValid()
    }
}
