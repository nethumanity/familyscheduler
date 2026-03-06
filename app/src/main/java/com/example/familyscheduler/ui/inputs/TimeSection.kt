package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.familyscheduler.domain.time.DurationDropdown
import com.example.familyscheduler.domain.time.FlexDropdown
import com.example.familyscheduler.domain.time.TimeDropdownPicker
import java.time.LocalTime

@Composable
fun TimeSection(
    startTime: LocalTime?,
    durationMinutes: Int,
    isFlexible: Boolean,
    flexMinutes: Int,
    onStartTimeChange: (LocalTime?) -> Unit,
    onDurationMinutesChange: (Int) -> Unit,
    onIsFlexibleChange: (Boolean) -> Unit,
    onFlexMinutesChange: (Int) -> Unit
) {

    TimeDropdownPicker(
        label = "開始",
        selectedTime = startTime,
        onTimeSelected = { onStartTimeChange(it) }
    )

    DurationDropdown(
        selectedMinutes = durationMinutes,
        onSelect = { onDurationMinutesChange(it) }
    )

    Row {
        Checkbox(
            checked = isFlexible,
            onCheckedChange = { onIsFlexibleChange(it) }
        )
        Text("時間をずらせる予定")
    }

    if (isFlexible) {
        FlexDropdown(
            selectedMinutes = flexMinutes,
            onSelect = { onFlexMinutesChange(it) }
        )
    }
}