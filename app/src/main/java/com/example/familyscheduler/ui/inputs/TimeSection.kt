package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.ui.utilities.TimeDropdownPicker
import com.example.familyscheduler.ui.utilities.StepDropdown
import java.time.LocalTime

@Composable
fun TimeSection(
    startTime: LocalTime?,
    durationSteps: Int,
    isFlexible: Boolean,
    backwardSteps: Int,
    forwardSteps: Int,
    onStartTimeChange: (LocalTime?) -> Unit,
    onDurationChange: (Int) -> Unit,
    onIsFlexibleChange: (Boolean) -> Unit,
    onBackwardChange: (Int) -> Unit,
    onForwardChange: (Int) -> Unit
) {

    TimeDropdownPicker(
        label = "開始",
        selectedTime = startTime,
        onTimeSelected = { onStartTimeChange(it) }
    )

    StepDropdown(
        label = "所要時間",
        selectedSteps = durationSteps,
        stepOptions = listOf(1, 2, 3, 4, 5, 6),
        onSelect = onDurationChange,
        modifier = Modifier
    )

    Row (
        verticalAlignment = Alignment.CenterVertically
    ){
        Checkbox(
            checked = isFlexible,
            onCheckedChange = { onIsFlexibleChange(it) }
        )
        Text("時間をずらせる予定")
    }

    if (isFlexible) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepDropdown(
                label = "前に",
                selectedSteps = backwardSteps,
                stepOptions = listOf(0, 1, 2, 3, 4, 5, 6),
                onSelect = onBackwardChange,
                modifier = Modifier.weight(1f)
            )
            StepDropdown(
                label = "後ろに",
                selectedSteps = forwardSteps,
                stepOptions = listOf(0, 1, 2, 3, 4, 5, 6),
                onSelect = onForwardChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}