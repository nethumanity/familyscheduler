package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    onOpenScheduleInput: () -> Unit,
    onBack: () -> Unit
) {
    Column {

        Button(onClick = onOpenScheduleInput) {
            Text("父母スケジュール登録")
        }

        Button(onClick = onBack) {
            Text("戻る")
        }
    }
}
