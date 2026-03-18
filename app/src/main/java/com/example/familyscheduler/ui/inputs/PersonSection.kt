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
import com.example.familyscheduler.domain.requirement.AllowedPersonOption

@Composable
fun PersonSection(
    isTwoPersonTask: Boolean,
    allowedPersonOption: AllowedPersonOption,
    onIsTwoPersonTaskChange: (Boolean) -> Unit,
    onAllowedPersonOptionChange: (AllowedPersonOption) -> Unit
) {

    Row (
        verticalAlignment = Alignment.CenterVertically
    ){
        Checkbox(
            checked = isTwoPersonTask,
            onCheckedChange = { onIsTwoPersonTaskChange(it) }
        )
        Text("2人で対応する予定")
    }

    if (!isTwoPersonTask) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AllowedPersonOption.values().forEach { option ->

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = allowedPersonOption == option,
                            onClick = { onAllowedPersonOptionChange(option) }
                        )
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = allowedPersonOption == option,
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