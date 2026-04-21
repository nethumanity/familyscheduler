@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.familyscheduler.ui.utilities

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.familyscheduler.domain.time.TimeAxis

@Composable
fun StepDropdown(
    label: String,
    selectedSteps: Int,
    stepMinutes: Int = TimeAxis.stepMinutes,
    stepOptions: List<Int>,
    onSelect: (Int) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "${selectedSteps * stepMinutes} 分",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            stepOptions.forEach { steps ->
                DropdownMenuItem(
                    text = { Text("${steps * stepMinutes} 分") },
                    onClick = {
                        expanded = false
                        onSelect(steps)
                    }
                )
            }
        }
    }
}