package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.ui.utilities.TimeDropdownPicker
import com.example.familyscheduler.ui.utilities.DayOfWeekUtilities
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel
import java.time.DayOfWeek

@Composable
fun ChildRoutineInputScreen(
    viewModel: ChildRoutineViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val form = uiState.form

    val focusManager = LocalFocusManager.current

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
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
                value = form.name,
                onValueChange = {
                    viewModel.updateName(it.replace("\n", ""))
                                }, //viewModel::updateName(it.replace("\n", "") },
                label = { Text("名前")},
                placeholder = { Text(text = "例：○○さん、長男、次女", color = Color.Gray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            TimeDropdownPicker(
                label = "起床",
                selectedTime = form.wakeUpTime,
                onTimeSelected = viewModel::updateWakeUpTime
            )
        }
        item {
            TimeDropdownPicker(
                label = "就寝",
                selectedTime = form.sleepTime,
                onTimeSelected = viewModel::updateSleepTime
            )
        }
        item {
            Text("保育園／幼稚園", fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {

                RadioButton(
                    selected = form.hasNursery,
                    onClick = { viewModel.updateHasNursery(true) }
                )
                Text("あり")

                Spacer(Modifier.width(16.dp))

                RadioButton(
                    selected = !form.hasNursery,
                    onClick = { viewModel.updateHasNursery(false) }
                )
                Text("なし")
            }

            Spacer(Modifier.height(16.dp))

            // ===== 保育園ありのときのみ =====
            if (form.hasNursery) {

                Text("登園日", fontWeight = FontWeight.Bold)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayOfWeek.entries.forEach { day ->

                        val selected = form.daysOfWeek.contains(day)

                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleDay(day) },
                            label = {
                                Text(
                                    text = DayOfWeekUtilities.short(day),
                                    color = DayOfWeekUtilities.color(day, MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("登園", fontWeight = FontWeight.Bold)

                TimeDropdownPicker(
                    label = "希望時刻",
                    selectedTime = form.nurseryStart,
                    onTimeSelected = viewModel::updateNurseryStart
                )

                TimeDropdownPicker(
                    label = "最早時刻（任意）",
                    selectedTime = form.nurseryStartEarliest,
                    onTimeSelected = viewModel::updateNurseryStartEarliest
                )

                TimeDropdownPicker(
                    label = "最遅時刻（任意）",
                    selectedTime = form.nurseryStartLatest,
                    onTimeSelected = viewModel::updateNurseryStartLatest
                )

                Spacer(Modifier.height(24.dp))

                Text("お迎え", fontWeight = FontWeight.Bold)

                TimeDropdownPicker(
                    label = "希望時刻",
                    selectedTime = form.nurseryEnd,
                    onTimeSelected = viewModel::updateNurseryEnd
                )

                TimeDropdownPicker(
                    label = "最早時刻（任意）",
                    selectedTime = form.nurseryEndEarliest,
                    onTimeSelected = viewModel::updateNurseryEndEarliest
                )

                TimeDropdownPicker(
                    label = "最遅時刻（任意）",
                    selectedTime = form.nurseryEndLatest,
                    onTimeSelected = viewModel::updateNurseryEndLatest
                )
            }
        }
        item{
            Spacer(Modifier.height(8.dp))

            val isValid = viewModel.isValid(form)

            Button(
                onClick = { viewModel.onSave() },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }


    }

    LaunchedEffect(viewModel) {
        viewModel.saveCompleted.collect {
            onSaved()
        }
    }
}