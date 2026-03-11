package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.time.TimeDropdownPicker
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel
import java.time.DayOfWeek

@Composable
fun ChildRoutineInputScreen(
    viewModel: ChildRoutineViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "子どもの登録",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Button(onClick = onBack) {
                Text("戻る")
            }
        }
        item {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("名前")},
                placeholder = { Text("例：○○さん、長男、次女", color = Color.Gray) }
            )
        }
        item {
            TimeDropdownPicker(
                label = "起床",
                selectedTime = state.wakeUpTime,
                onTimeSelected = viewModel::updateWakeUpTime
            )
        }
        item {
            TimeDropdownPicker(
                label = "就寝",
                selectedTime = state.sleepTime,
                onTimeSelected = viewModel::updateSleepTime
            )
        }
        item {
            Text("保育園／幼稚園")

            Row(verticalAlignment = Alignment.CenterVertically) {

                RadioButton(
                    selected = state.hasNursery,
                    onClick = { viewModel.updateHasNursery(true) }
                )
                Text("あり")

                Spacer(Modifier.width(16.dp))

                RadioButton(
                    selected = !state.hasNursery,
                    onClick = { viewModel.updateHasNursery(false) }
                )
                Text("なし")
            }

            Spacer(Modifier.height(16.dp))

            // ===== 保育園ありのときのみ =====
            if (state.hasNursery) {

                Text("登園日")

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayOfWeek.entries.forEach { day ->

                        val color =
                            when(day) {
                                DayOfWeek.SATURDAY -> Color.Blue
                                DayOfWeek.SUNDAY -> Color.Red
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                        val selected = state.daysOfWeek.contains(day)

                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleDay(day) },
                            label = { Text(text = viewModel.dayLabels[day] ?: "", color = color) }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("登園")

                TimeDropdownPicker(
                    label = "希望時刻",
                    selectedTime = state.nurseryStart,
                    onTimeSelected = viewModel::updateNurseryStart
                )

                TimeDropdownPicker(
                    label = "最早時刻（任意）",
                    selectedTime = state.nurseryStartEarliest,
                    onTimeSelected = viewModel::updateNurseryStartEarliest
                )

                TimeDropdownPicker(
                    label = "最遅時刻（任意）",
                    selectedTime = state.nurseryStartLatest,
                    onTimeSelected = viewModel::updateNurseryStartLatest
                )

                Spacer(Modifier.height(24.dp))

                Text("お迎え")

                TimeDropdownPicker(
                    label = "希望時刻",
                    selectedTime = state.nurseryEnd,
                    onTimeSelected = viewModel::updateNurseryEnd
                )

                TimeDropdownPicker(
                    label = "最早時刻（任意）",
                    selectedTime = state.nurseryEndEarliest,
                    onTimeSelected = viewModel::updateNurseryEndEarliest
                )

                TimeDropdownPicker(
                    label = "最遅時刻（任意）",
                    selectedTime = state.nurseryEndLatest,
                    onTimeSelected = viewModel::updateNurseryEndLatest
                )
            }
        }
        item{
            Spacer(Modifier.height(8.dp))

            val isValid =
                if (state.hasNursery) {
                    state.name.isNotBlank() &&
                            state.wakeUpTime != null &&
                            state.sleepTime != null &&
                            state.daysOfWeek.isNotEmpty() &&
                            state.nurseryStart != null &&
                            state.nurseryEnd != null
                } else {
                    state.name.isNotBlank() &&
                            state.wakeUpTime != null &&
                            state.sleepTime != null
                }

            Button(
                onClick = { viewModel.onSave() },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }


    }

    LaunchedEffect(Unit) {
        viewModel.saveCompleted.collect {
            onSaved()
        }
    }
}