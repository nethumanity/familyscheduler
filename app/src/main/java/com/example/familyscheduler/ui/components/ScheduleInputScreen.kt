package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    var title by remember { mutableStateOf("") }

    var startHour by remember { mutableStateOf("9") }
    var startMinute by remember { mutableStateOf("0") }

    var endHour by remember { mutableStateOf("17") }
    var endMinute by remember { mutableStateOf("0") }

    var selectedPerson by remember { mutableStateOf(Person.FATHER) }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Text("スケジュール入力", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // 名前入力
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("タイトル") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Person選択
        Text("対象者")

        Row {

            Person.entries.forEach { person ->

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = selectedPerson == person,
                        onClick = { selectedPerson = person }
                    )

                    Text(person.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 開始時刻
        Text("開始時刻")

        Row {

            OutlinedTextField(
                value = startHour,
                onValueChange = { startHour = it },
                label = { Text("時") },
                modifier = Modifier.width(80.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = startMinute,
                onValueChange = { startMinute = it },
                label = { Text("分") },
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 終了時刻
        Text("終了時刻")

        Row {

            OutlinedTextField(
                value = endHour,
                onValueChange = { endHour = it },
                label = { Text("時") },
                modifier = Modifier.width(80.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = endMinute,
                onValueChange = { endMinute = it },
                label = { Text("分") },
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                val start = LocalTime.of(
                    startHour.toIntOrNull() ?: 0,
                    startMinute.toIntOrNull() ?: 0
                )

                val end = LocalTime.of(
                    endHour.toIntOrNull() ?: 0,
                    endMinute.toIntOrNull() ?: 0
                )

                val type = ScheduleType(
                    id = UUID.randomUUID(),
                    title = title,
                    category = StateCategory.WORK
                )

                val schedule = ScheduleTemplate(
                    person = selectedPerson,
                    title = title,
                    type = type,
                    timeRange = TimeRange(start, end),
                    repeatRule = RepeatRule.Daily
                )

                val template = DailyTemplate(
                    id = UUID.randomUUID(),
                    person = selectedPerson,
                    name = title,
                    schedules = listOf(schedule),
                    repeatRule = RepeatRule.Daily
                )

                viewModel.saveTemplate(template)

                onSaved()
            }
        ) {

            Text("保存")
        }
    }
}

/*元に戻すセーフ版
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

 */
