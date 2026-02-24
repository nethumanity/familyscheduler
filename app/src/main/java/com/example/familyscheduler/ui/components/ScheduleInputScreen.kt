package com.example.familyscheduler.ui.components

import android.R.attr.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.viewmodel.TemplateEditViewModel
import java.time.DayOfWeek

@Composable
fun ScheduleInputScreen(
    viewModel: TemplateEditViewModel = viewModel(),
    onSaved: () -> Unit,
    onBack: () -> Unit,
    paddingValues: PaddingValues = PaddingValues()
) {

    // ===== 対象者 =====
    var selectedPerson by remember {
        mutableStateOf(Person.FATHER)
    }

    // ===== スケジュール名 =====
    var templateName by remember {
        mutableStateOf("")
    }

    // ===== RepeatRule =====
    var useWeeklyRule by remember {
        mutableStateOf(false)
    }

    var selectedDays by remember {
        mutableStateOf(setOf<DayOfWeek>())
    }

    // ===== 固定スケジュール =====
    val fixedSchedules = remember {
        mutableStateListOf<ScheduleTemplate>()
    }

    // ===== 追加スケジュール =====
    val additionalSchedules = remember {
        mutableStateListOf<ScheduleTemplate>()
    }

    // ===== メインレイアウト =====
    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),

        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),

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

                            Text(person.label)
                        }
                    }
                }
            }
        }

        // ===== Template名 =====

        item {

            OutlinedTextField(
                value = templateName,
                onValueChange = { templateName = it },
                label = { Text("スケジュール名") },
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
                        checked = useWeeklyRule,
                        onCheckedChange = {
                            useWeeklyRule = it
                        }
                    )

                    Text("曜日を指定する")
                }

                if (useWeeklyRule) {

                    LazyVerticalGrid(

                        columns = GridCells.Fixed(2),

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),

                        verticalArrangement = Arrangement.spacedBy(4.dp),

                        horizontalArrangement = Arrangement.spacedBy(4.dp)

                    ) {

                        items(DayOfWeek.entries.size) { index ->

                            val day = DayOfWeek.entries[index]

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Checkbox(
                                    checked = selectedDays.contains(day),
                                    onCheckedChange = {

                                        selectedDays =
                                            if (selectedDays.contains(day))
                                                selectedDays - day
                                            else
                                                selectedDays + day
                                    }
                                )

                                Text(day.name.take(3))
                            }
                        }
                    }
                }
            }
        }

        // ===== 固定スケジュール =====

        item {

            FixedScheduleSection(
                person = selectedPerson,
                schedules = fixedSchedules
            )
        }

        // ===== 追加スケジュール =====

        item {

            AdditionalScheduleSection(
                person = selectedPerson,
                schedules = additionalSchedules
            )
        }

        // ===== 保存 =====

        item {

            Button(

                modifier = Modifier.fillMaxWidth(),

                onClick = {

                    val repeatRule =
                        if (useWeeklyRule)
                            RepeatRule.Weekly(selectedDays)
                        else
                            RepeatRule.Daily

                    val normalizedSchedules =
                        TemplateNormalizer.normalize(
                            fixedSchedules + additionalSchedules
                        )

                    val template = DailyTemplate(
                        person = selectedPerson,
                        name = templateName,
                        schedules = normalizedSchedules,
                        repeatRule = repeatRule
                    )

                    viewModel.saveTemplate(template)

                    onSaved()
                }

            ) {
                Text("保存")
            }
        }
    }
}

/*
@Composable
fun ScheduleInputScreen(
    viewModel: TemplateEditViewModel = viewModel(),
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(StateCategory.WORK)
    }
    var startTime by remember {
        mutableStateOf(LocalTime.of(9, 0))
    }
    var endTime by remember {
        mutableStateOf(LocalTime.of(17, 0))
    }
    var selectedDays by remember {
        mutableStateOf(setOf<DayOfWeek>())
    }
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 戻るボタン
        Button(onClick = onBack) {
            Text("戻る")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // タイトル入力
        Text("タイトル")
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("例：仕事、通勤、睡眠") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        // カテゴリ選択
        Text("カテゴリ")
        StateCategory.values().forEach { category ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                    }
                )
                Text(category.name)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 曜日選択
        Text("曜日")
        DayOfWeek.values().forEach { day ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedDays.contains(day),
                    onCheckedChange = {
                        selectedDays =
                            if (it)
                                selectedDays + day
                            else
                                selectedDays - day
                    }
                )
                Text(day.name)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 保存ボタン
        Button(
            onClick = {
                val type = ScheduleType(
                    id = UUID.randomUUID(),
                    title = title,
                    category = selectedCategory
                )
                val schedule = ScheduleTemplate(
                    person = Person.FATHER,
                    title = title,
                    type = type,
                    timeRange = TimeRange(
                        start = startTime,
                        end = endTime
                    ),
                    repeatRule = RepeatRule.Weekly(selectedDays)
                )
                val template = DailyTemplate(
                    id = UUID.randomUUID(),
                    person = Person.FATHER,
                    name = title,
                    schedules = listOf(schedule),
                    repeatRule = RepeatRule.Weekly(selectedDays)
                )
                viewModel.saveTemplate(template)
                onSaved()
            }
        ) {
            Text("保存")
        }
    }
}

/*
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

 */