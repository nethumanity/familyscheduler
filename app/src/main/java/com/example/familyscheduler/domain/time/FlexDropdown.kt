@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.familyscheduler.domain.time

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

@Composable
fun FlexDropdown(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(30, 60, 90, 120, 150, 180)

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = "${selectedMinutes}分",
            onValueChange = {},
            readOnly = true,
            label = { Text("") },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text("${minutes}分") },
                    onClick = {
                        expanded = false
                        onSelect(minutes)
                    }
                )
            }
        }
    }
}