package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.familyscheduler.domain.slot.SlotState

@Composable
fun TaskSection(
    taskName: String,
    targetState: SlotState,
    onTaskNameChange: (String) -> Unit,
    onTargetStateChange: (SlotState) -> Unit
) {

    OutlinedTextField(
        value = taskName,
        onValueChange = onTaskNameChange,
        label = { Text("予定名") }
    )

    Row {
        RadioButton(
            selected = targetState == SlotState.LIFE,
            onClick = { onTargetStateChange(SlotState.LIFE) }
        )
        Text("家事・用事・食事")

        RadioButton(
            selected = targetState == SlotState.CHILDCARE,
            onClick = { onTargetStateChange(SlotState.CHILDCARE) }
        )
        Text("育児")
    }
}