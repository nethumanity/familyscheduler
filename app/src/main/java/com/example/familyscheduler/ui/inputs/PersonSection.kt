package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.familyscheduler.viewmodel.OneTimeAppointmentViewModel

@Composable
fun PersonSection(
    state: OneTimeAppointmentViewModel.OneTimeAppointmentInput,
    viewModel: OneTimeAppointmentViewModel
) {

    Row {
        Checkbox(
            checked = state.isTwoPersonTask,
            onCheckedChange = { viewModel.updateTwoPerson(it) }
        )
        Text("2人で対応する予定")
    }

    if (!state.isTwoPersonTask) {
        Row {
            OneTimeAppointmentViewModel.AllowedPersonOption.values().forEach { option ->
                RadioButton(
                    selected = state.allowedPersonOption == option,
                    onClick = { viewModel.updateAllowedPerson(option) }
                )
                Text("$(option.label)")
            }
        }
    }
}