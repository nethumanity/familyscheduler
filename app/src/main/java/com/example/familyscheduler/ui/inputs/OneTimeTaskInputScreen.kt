package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.viewmodel.OneTimeTaskViewModel

@Composable
fun OneTimeTaskInputScreen(
    viewModel: OneTimeTaskViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
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
                text = "日付指定で予定を追加",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Button(onClick = onBack) {
                Text("戻る")
            }
        }

        item { DateSection(state, viewModel) }

        item {
            TaskSection(
                taskName = state.taskName,
                targetState = state.targetState,
                onTaskNameChange = { viewModel.updateTaskName(it) },
                onTargetStateChange = { viewModel.updateTargetState(it) }
            )
        }

        item {
            PersonSection(
                isTwoPersonTask = state.isTwoPersonTask,
                allowedPersonOption = state.allowedPersonOption,
                onIsTwoPersonTaskChange = { viewModel.updateTwoPerson(it) },
                onAllowedPersonOptionChange = {viewModel.updateAllowedPerson(it) }
            )
        }

        item {
            TimeSection(
                startTime = state.startTime,
                durationMinutes = state.durationMinutes,
                isFlexible = state.isFlexible,
                flexMinutes = state.flexMinutes,
                onStartTimeChange = {viewModel.updateStartTime(it) },
                onDurationMinutesChange = { viewModel.updateDuration(it) },
                onIsFlexibleChange = { viewModel.updateFlexible(it) },
                onFlexMinutesChange = { viewModel.updateFlex(it) }
            )
        }

        item {
            Spacer(Modifier.height(8.dp))

            val isValid= state.taskName.isNotBlank()

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