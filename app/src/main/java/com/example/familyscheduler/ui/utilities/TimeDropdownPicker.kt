@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.familyscheduler.ui.utilities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime

@Composable
fun TimeDropdownPicker(
    label: String,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    val hours = (0..23).toList()
    val minutes = listOf(0, 30)

    var hourExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }

    val selectedHour = selectedTime?.hour ?: 0
    val selectedMinute = selectedTime?.minute ?: 0

    Column {
        Text(label)

        Row (
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ===== 時 =====
            ExposedDropdownMenuBox(
                expanded = hourExpanded,
                onExpandedChange = { hourExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedHour.toString().padStart(2, '0'),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("時") },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = hourExpanded,
                    onDismissRequest = { hourExpanded = false }
                ) {
                    hours.forEach { hour ->
                        DropdownMenuItem(
                            text = {
                                Text(hour.toString().padStart(2, '0'))
                            },
                            onClick = {
                                hourExpanded = false
                                onTimeSelected(
                                    LocalTime.of(hour, selectedMinute)
                                )
                            }
                        )
                    }
                }
            }

            // ===== 分 =====
            ExposedDropdownMenuBox(
                expanded = minuteExpanded,
                onExpandedChange = { minuteExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedMinute.toString().padStart(2, '0'),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("分") },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = minuteExpanded,
                    onDismissRequest = { minuteExpanded = false }
                ) {
                    minutes.forEach { minute ->
                        DropdownMenuItem(
                            text = {
                                Text(minute.toString().padStart(2, '0'))
                            },
                            onClick = {
                                minuteExpanded = false
                                onTimeSelected(
                                    LocalTime.of(selectedHour, minute)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
