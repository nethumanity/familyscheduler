package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.schedule.ScheduleType
import com.example.familyscheduler.ui.utilities.TimeDropdownPicker
import com.example.familyscheduler.viewmodel.TemplateEditViewModel

@Composable
fun AdditionalScheduleSection(
    state: TemplateEditViewModel.TemplateEditUiState,
    viewModel: TemplateEditViewModel
) {

    Column {

        Text(text = "追加ルーティン", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        state.additionalSchedules.forEachIndexed { index, schedule ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("カテゴリ")

                ScheduleType.additionalAllowedTypes
                    .chunked(3)
                    .forEach { rowTypes ->

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            rowTypes.forEach { type ->

                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    RadioButton(
                                        selected = schedule.type == type,
                                        onClick = {
                                            viewModel.updateAdditionalType(index, type)
                                        }
                                    )

                                    Text(type.title)
                                }
                            }
                        }
                    }

                Spacer(Modifier.height(6.dp))

                TimeDropdownPicker(
                    label = "開始",
                    selectedTime = schedule.start
                ) {
                    viewModel.updateAdditionalStart(index, it)
                }

                TimeDropdownPicker(
                    label = "終了",
                    selectedTime = schedule.end
                ) {
                    viewModel.updateAdditionalEnd(index, it)
                }

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = {
                        viewModel.removeAdditionalSchedule(index)
                    }
                ) {
                    Text("削除")
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        Button(
            onClick = { viewModel.addAdditionalSchedule() }
        ) {
            Text("＋追加")
        }
    }
}
