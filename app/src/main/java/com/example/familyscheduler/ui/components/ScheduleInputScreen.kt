package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.ScheduleType
import com.example.familyscheduler.domain.schedule.StateCategory
import com.example.familyscheduler.domain.schedule.TimeRange
import com.example.familyscheduler.viewmodel.TemplateEditViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

@Composable
fun ScheduleInputScreen(
    viewModel: TemplateEditViewModel = viewModel(),
    onSaved: () -> Unit
) {

    Column {

        Button(
            onClick = {

                val workType = ScheduleType(
                    id = UUID.randomUUID(),
                    title = "仕事",
                    category = StateCategory.WORK
                )

                val schedule = ScheduleTemplate(
                    person = Person.FATHER,
                    title = "仕事",
                    type = workType,
                    timeRange = TimeRange(
                        start = LocalTime.of(9, 0),
                        end = LocalTime.of(17, 0)
                    ),
                    repeatRule = RepeatRule.Weekly(
                        setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY
                        )
                    )
                )

                val group = DailyTemplate(
                    id = UUID.randomUUID(),
                    person = Person.FATHER,
                    name = "父 平日",
                    schedules = listOf(schedule),
                    repeatRule = RepeatRule.Weekly(
                        setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY
                        )
                    )
                )

                viewModel.saveTemplate(group)

                onSaved()
            }
        ) {
            Text("父の平日スケジュールを保存")
        }
    }
}
