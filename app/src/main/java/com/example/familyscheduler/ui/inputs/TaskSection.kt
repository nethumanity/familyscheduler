package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.viewmodel.OneTimeAppointmentViewModel

@Composable
fun TaskSection(
    state: OneTimeAppointmentViewModel.OneTimeAppointmentInput,
    viewModel: OneTimeAppointmentViewModel
) {

    OutlinedTextField(
        value = state.taskName,
        onValueChange = { viewModel.updateTaskName(it) },
        label = { Text("予定名") }
    )

    Row {
        RadioButton(
            selected = state.targetState == SlotState.LIFE,
            onClick = { viewModel.updateTargetState(SlotState.LIFE) }
        )
        Text("家事・用事・食事")

        RadioButton(
            selected = state.targetState == SlotState.CHILDCARE,
            onClick = { viewModel.updateTargetState(SlotState.CHILDCARE) }
        )
        Text("育児")
    }
}