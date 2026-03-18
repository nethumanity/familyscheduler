package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.ui.utilities.labelForInput

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
        label = { Text("予定名") },
        //placeholder = { Text("例：XXX / XXX / XXX") },
        modifier = Modifier.fillMaxWidth()
    )

    SlotState.taskInputAllowedState
        .chunked(2)
        .forEach { rowStates ->

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                rowStates.forEach { state ->

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = targetState == state,
                            onClick = { onTargetStateChange(state) }
                        )

                        Text(labelForInput(state))
                    }
                }
            }
        }
    /*
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

     */
}