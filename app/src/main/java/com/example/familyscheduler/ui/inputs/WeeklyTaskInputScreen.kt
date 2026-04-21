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
import com.example.familyscheduler.viewmodel.WeeklyTaskViewModel

@Composable
fun WeeklyTaskInputScreen(
    viewModel: WeeklyTaskViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "毎週（毎日）の予定を追加",
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
            TaskSection(
                taskName = state.taskName,
                targetState = state.targetState,
                onTaskNameChange = { viewModel.updateTaskName(it) },
                onTargetStateChange = { viewModel.updateTargetState(it) }
            )
        }
        item {
            DaysOfWeekSection(
                everyDay = state.everyDay,
                selectedDays = state.daysOfWeek,
                onToggleEveryDay = {
                    viewModel.toggleEveryDay(it)
                },
                onToggleDay = {
                    viewModel.toggleDay(it)
                }
            )
        }
        item {
            PersonSection(
                isTwoPersonTask = state.isTwoPersonTask,
                allowedPersonOption = state.allowedPersonOption,
                onIsTwoPersonTaskChange = { viewModel.updateTwoPerson(it)},
                onAllowedPersonOptionChange = {viewModel.updateAllowedPerson(it)}
            )
        }
        item {
            TimeSection(
                startTime = state.startTime,
                durationSteps = state.durationSteps,
                isFlexible = state.isFlexible,
                backwardSteps = state.flexBackwardSteps,
                forwardSteps = state.flexForwardSteps,
                onStartTimeChange = { viewModel.updateStartTime(it) },
                onDurationChange = { viewModel.updateDuration(it) },
                onIsFlexibleChange = { viewModel.updateFlexible(it) },
                onBackwardChange = { viewModel.updateBackwardFlex(it) },
                onForwardChange = { viewModel.updateForwardFlex(it) }
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