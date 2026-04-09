package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.ui.utilities.labelForInput

@Composable
fun TaskSection(
    taskName: String,
    targetState: SlotState,
    onTaskNameChange: (String) -> Unit,
    onTargetStateChange: (SlotState) -> Unit
) {

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = taskName,
        onValueChange = { onTaskNameChange(it.replace("\n", "")) },
        label = { Text("予定名") },
        placeholder = { Text(text = "例：買い物 / 食事準備 / お風呂 / 通院", color = Color.Gray) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),
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
}