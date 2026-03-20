@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.familyscheduler.domain.time

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

    var selectedHour by remember {
        mutableStateOf(selectedTime?.hour ?: 0)
    }
    var selectedMinute by remember {
        mutableStateOf(selectedTime?.minute ?: 0)
    }

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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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
                                selectedHour = hour
                                hourExpanded = false
                                onTimeSelected(
                                    LocalTime.of(selectedHour, selectedMinute)
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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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
                                selectedMinute = minute
                                minuteExpanded = false
                                onTimeSelected(
                                    LocalTime.of(selectedHour, selectedMinute)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
