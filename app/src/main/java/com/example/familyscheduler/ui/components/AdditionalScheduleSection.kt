package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.StateCategory

@Composable
fun AdditionalScheduleSection(
    schedules: MutableList<ScheduleTemplate>
) {

    var showEditor by remember {
        mutableStateOf(false)
    }

    var routineName by remember {
        mutableStateOf("")
    }

    Column {

        Button(
            onClick = {
                showEditor = true
            }
        ) {
            Text("スケジュールを追加")
        }

        if (showEditor) {

            var category by remember {
                mutableStateOf(StateCategory.WORK)
            }

            Column {

                StateCategory.entries.forEach {

                    Row {

                        RadioButton(
                            selected = category == it,
                            onClick = { category = it }
                        )

                        Text(it.name)
                    }
                }

                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("ルーティン名") },
                    modifier = Modifier.fillMaxWidth()
                )

                TimeRangeEditor(
                    category,
                    routineName,
                    schedules
                )
            }
        }
    }
}