package com.example.familyscheduler.ui.inputs

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.time.TimeDropdownPicker
import com.example.familyscheduler.ui.utilities.DayOfWeekUtilities
import com.example.familyscheduler.viewmodel.TemplateEditViewModel
import java.time.DayOfWeek

@Composable
fun ScheduleInputScreen(
    viewModel: TemplateEditViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),

        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {

        item {

            Text(
                text = "父母の基本スケジュールを登録",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Button(onClick = onBack) {
                Text("戻る")
            }
        }

        // ===== Person選択 =====

        item {

            Column {

                Text(text = "対象者", fontWeight = FontWeight.Bold)

                Row {

                    Person.entries.forEach { person ->

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected = state.person == person,
                                onClick = { viewModel.updatePerson(person) }
                            )

                            Text(person.label)
                        }
                    }
                }
            }
        }

        // ===== Template名 =====

        item {

            OutlinedTextField(
                value = state.templateName,
                onValueChange = { viewModel.updateTemplateName(it) },
                label = { Text("スケジュール名") },
                placeholder = { Text("例：出勤 / 在宅 / 休暇") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ===== RepeatRule =====

        item {

            Column {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = state.noWeeklyRule,
                        onCheckedChange = {
                            viewModel.updateNoWeeklyRule(it)
                        }
                    )

                    Text("曜日を指定しない")
                }

                if (!state.noWeeklyRule) {      //引数に

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        DayOfWeek.entries.forEach { day ->

                            FilterChip(
                                selected = state.selectedDays.contains(day),    //引数に
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
                }
            }
        }

        // ===== 固定スケジュール =====

        item {

            Text(text = "仕事", fontWeight = FontWeight.Bold)

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.noWork,
                    onCheckedChange = { viewModel.updateNoWork(it) }
                )

                Text("なし")
            }

            if (!state.noWork) {

                TimeDropdownPicker(
                    label = "開始",
                    selectedTime = state.workStart
                ) {
                    viewModel.updateWorkStart(it)
                }

                TimeDropdownPicker(
                    label = "終了",
                    selectedTime = state.workEnd
                ) {
                    viewModel.updateWorkEnd(it)
                }
                Log.d("UI", "workStart=${state.workStart}")
            }
        }

        item {

            Text(text = "往路通勤", fontWeight = FontWeight.Bold)

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.noGoCommute,
                    onCheckedChange = { viewModel.updateNoGoCommute(it) }
                )

                Text("なし")
            }

            if (!state.noGoCommute) {

                TimeDropdownPicker(
                    label = "開始",
                    selectedTime = state.goCommuteStart
                ) {
                    viewModel.updateGoCommuteStart(it)
                }

                TimeDropdownPicker(
                    label = "終了",
                    selectedTime = state.goCommuteEnd
                ) {
                    viewModel.updateGoCommuteEnd(it)
                }
            }
        }

        item {

            Text(text = "復路通勤", fontWeight = FontWeight.Bold)

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.noBackCommute,
                    onCheckedChange = { viewModel.updateNoBackCommute(it) }
                )

                Text("なし")
            }

            if (!state.noBackCommute) {

                TimeDropdownPicker(
                    label = "開始",
                    selectedTime = state.backCommuteStart
                ) {
                    viewModel.updateBackCommuteStart(it)
                }

                TimeDropdownPicker(
                    label = "終了",
                    selectedTime = state.backCommuteEnd
                ) {
                    viewModel.updateBackCommuteEnd(it)
                }
            }
        }

        item {

            Text(text = "睡眠", fontWeight = FontWeight.Bold)

            TimeDropdownPicker(
                label = "開始",
                selectedTime = state.sleepStart
            ) {
                viewModel.updateSleepStart(it)
            }

            TimeDropdownPicker(
                label = "終了",
                selectedTime = state.sleepEnd
            ) {
                viewModel.updateSleepEnd(it)
            }
        }

        // ===== 追加スケジュール =====

        item {

            AdditionalScheduleSection(
                state = state,
                viewModel = viewModel
            )
        }

        // ===== 重複警告 =====

        item {
            if (state.overlaps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "⚠ 時間が重複しています",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )

                    state.overlaps.take(3).forEach { (a, b) ->
                        Text(
                            text =
                                "${a.type.title} (${a.timeRange.start}〜${a.timeRange.end}) " +
                                        "と " +
                                        "${b.type.title} (${b.timeRange.start}〜${b.timeRange.end})",
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }

                    if (state.overlaps.size > 3) {
                        Text(
                            text = "他 ${state.overlaps.size - 3} 件",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // ===== 保存 =====

        item {
            Spacer(Modifier.height(8.dp))

            val isValid = viewModel.isValid(state)

            Button(

                modifier = Modifier.fillMaxWidth(),
                enabled = isValid,
                onClick = { viewModel.saveTemplate() }

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