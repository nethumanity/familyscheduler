package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    //viewModel: SettingsViewModel,
    onOpenScheduleInput: () -> Unit,
    onOpenChildRoutineInput: () -> Unit,
    onOpenTaskInput: () -> Unit,
    onBack: () -> Unit
) {
    //val uiState by viewModel.uiState.collectAsState()

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "設定　※現在は各種登録のみ可",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Button(onClick = onBack) {
                Text("戻る")
            }
        }
        item { SettingsSectionTitle("各種登録") }
        item {
            SettingsItem(
                title = "父母のスケジュールを追加",
                onClick = onOpenScheduleInput
            )
        }
        item {
            SettingsItem(
                title = "子どもの登録",
                onClick = onOpenChildRoutineInput
            )
        }
        item {
            SettingsItem(
                title = "予定を追加",
                onClick = onOpenTaskInput
            )
        }
        item { SettingsSectionTitle("パラメータ") }
        item {
            SettingsNumberItem(
                title = "1人で見れる子どもの数",
                value = 2,
                onValueChange = { /* ViewModel */ }
            )
        }
        item {
            SettingsNumberItem(
                title = "寝かしつけ所用時間（分）",
                value = 30,
                onValueChange = { }
            )
        }
        item {
            SettingsNumberItem(
                title = "登園所用時間（分）",
                value = 30,
                onValueChange = { }
            )
        }
        item {
            SettingsNumberItem(
                title = "お迎え所用時間（分）",
                value = 30,
                onValueChange = { }
            )
        }
        item { SettingsSectionTitle("表示") }
        item {
            SettingsSwitchItem(
                title = "凡例を表示",
                checked = false,
                onCheckedChange = { }
            )
        }
        item {
            SettingsSwitchItem(
                title = "集計を表示",
                checked = false,
                onCheckedChange = { }
            )
        }
    }
}
