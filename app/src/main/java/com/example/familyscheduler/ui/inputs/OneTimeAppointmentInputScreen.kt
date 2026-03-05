package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.viewmodel.OneTimeAppointmentViewModel

@Composable
fun OneTimeAppointmentInputScreen(
    viewModel: OneTimeAppointmentViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Text("日付指定で予定を追加")

        Spacer(Modifier.height(8.dp))

        Button(onClick = onBack) {
            Text("戻る")
        }

        Spacer(Modifier.height(16.dp))

        DateSection(state, viewModel)

        Spacer(Modifier.height(16.dp))

        TaskSection(state, viewModel)

        Spacer(Modifier.height(16.dp))

        PersonSection(state, viewModel)

        Spacer(Modifier.height(16.dp))

        TimeSection(state, viewModel)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onSave() },
            enabled = viewModel.isValid()
        ) {
            Text("保存")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveCompleted.collect {
            onSaved()
        }
    }
}