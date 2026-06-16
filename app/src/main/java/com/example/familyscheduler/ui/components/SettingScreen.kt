package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenScheduleInput: () -> Unit,
    onOpenChildRoutineInput: () -> Unit,
    onOpenTaskInput: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val step = TimeAxis.stepMinutes

    fun durationText(value: Int): String {
        val minutes = value * step
        return if (minutes == 0) "設定しない" else "$minutes 分"
    }

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "設定",
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
                value = uiState.maxChildrenPerAdult,
                displayText = "${uiState.maxChildrenPerAdult} 人",
                onValueChange = viewModel::updateMaxChildren
            )
        }
        item {
            SettingsNumberItem(
                title = "登園所用時間",
                value = uiState.dropOffSteps,
                displayText = durationText(uiState.dropOffSteps),
                onValueChange = viewModel::updateDropOff
            )
        }
        item {
            SettingsNumberItem(
                title = "お迎え所用時間",
                value = uiState.pickupSteps,
                displayText = durationText(uiState.pickupSteps),
                onValueChange = viewModel::updatePickup
            )
        }
        item {
            SettingsNumberItem(
                title = "寝かしつけ所用時間",
                value = uiState.bedtimeSteps,
                displayText = durationText(uiState.bedtimeSteps),
                onValueChange = viewModel::updateBedtime
            )
        }
        item { SettingsSectionTitle("表示") }
        item {
            SettingsSwitchItem(
                title = "凡例を表示",
                checked = uiState.showLegend,
                onCheckedChange = { viewModel.toggleLegend() }
            )
        }
        item {
            SettingsSwitchItem(
                title = "集計を表示",
                checked = uiState.showTotal,
                onCheckedChange = { viewModel.toggleTotal() }
            )
        }
        item {
            SettingsNumberItem(
                title = "表示開始時刻",
                value = uiState.timelineStartIndex,
                displayText = TimeAxis.timeLabelAt(uiState.timelineStartIndex),
                onValueChange = viewModel::updateTimelineStartIndex
            )
        }
        item {
            SettingsNumberItem(
                title = "表示終了時刻",
                value = uiState.timelineEndIndex,
                displayText = TimeAxis.timeLabelAt(uiState.timelineEndIndex + 1),
                onValueChange = viewModel::updateTimelineEndIndex
            )
        }
        item {  // 製品版では表示しない、あるいは、削除？
            SettingsNumberItem(
                title = "単位時間",
                value = uiState.timelineStepMinutes,
                displayText = "${uiState.timelineStepMinutes} 分（固定）",
                onValueChange = {} // viewModel未実装
            )
        }
    }
}
