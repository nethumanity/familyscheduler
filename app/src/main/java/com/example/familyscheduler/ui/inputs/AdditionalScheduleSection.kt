package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.time.TimeDropdownPicker
import com.example.familyscheduler.viewmodel.TemplateEditViewModel

@Composable
fun AdditionalScheduleSection(
    state: TemplateEditViewModel.TemplateEditUiState,
    viewModel: TemplateEditViewModel
) {

    Column {

        Text(text = "追加ルーティン", fontWeight = FontWeight.Bold)

        state.additionalSchedules.forEachIndexed { index, schedule ->

            TimeDropdownPicker(
                label = "開始",
                selectedTime = schedule.timeRange.start
            ) {
                viewModel.updateAdditionalStart(index, it)
            }

            TimeDropdownPicker(
                label = "終了",
                selectedTime = schedule.timeRange.end
            ) {
                viewModel.updateAdditionalEnd(index, it)
            }

            Button(
                onClick = {
                    viewModel.removeAdditionalSchedule(index)
                }
            ) {
                Text("削除")
            }

            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.addAdditionalSchedule() }
        ) {
            Text("＋追加")
        }
    }
}


/*
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

                TimeDropdownPicker(
                    label = "開始",
                    selectedTime = state.start
                ) {
                    viewModel.updateWorkStart(it)
                }

                TimeDropdownPicker(
                    label = "終了",
                    selectedTime = state.end
                ) {
                    viewModel.updateWorkEnd(it)
                }
            }
        }
    }
}

 */