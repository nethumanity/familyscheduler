package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OneTimeAppointmentViewModel.AllowedPersonOption.values().forEach { option ->

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = state.allowedPersonOption == option,
                            onClick = {
                                viewModel.updateAllowedPerson(option)
                            }
                        )
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.allowedPersonOption == option,
                        onClick = null
                    )

                    Text(
                        text = option.label,
                        maxLines = 1
                    )
                }
            }
        }
    }
}