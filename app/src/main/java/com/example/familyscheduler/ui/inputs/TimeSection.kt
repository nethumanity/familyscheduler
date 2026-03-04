package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.familyscheduler.domain.time.DurationDropdown
import com.example.familyscheduler.domain.time.FlexDropdown
import com.example.familyscheduler.domain.time.TimeDropdownPicker
import com.example.familyscheduler.viewmodel.OneTimeAppointmentViewModel

@Composable
fun TimeSection(
    state: OneTimeAppointmentViewModel.OneTimeAppointmentInput,
    viewModel: OneTimeAppointmentViewModel
) {

    TimeDropdownPicker(
        label = "開始",
        selectedTime = state.startTime,
        onTimeSelected = { viewModel.updateStartTime(it) }
    )

    DurationDropdown(
        selectedMinutes = state.durationMinutes,
        onSelect = { viewModel.updateDuration(it) }
    )

    Row {
        Checkbox(
            checked = state.isFlexible,
            onCheckedChange = { viewModel.updateFlexible(it) }
        )
        Text("時間をずらせる予定")
    }

    if (state.isFlexible) {
        FlexDropdown(
            selectedMinutes = state.flexMinutes,
            onSelect = { viewModel.updateFlex(it) }
        )
    }
}