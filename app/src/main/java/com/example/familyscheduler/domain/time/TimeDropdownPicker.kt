@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.familyscheduler.domain.time

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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

        Row {

            // ===== 時 =====
            ExposedDropdownMenuBox(
                expanded = hourExpanded,
                onExpandedChange = { hourExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedHour.toString().padStart(2, '0'),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("時") },
                    modifier = Modifier.menuAnchor()
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

            Spacer(Modifier.width(8.dp))

            // ===== 分 =====
            ExposedDropdownMenuBox(
                expanded = minuteExpanded,
                onExpandedChange = { minuteExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedMinute.toString().padStart(2, '0'),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("分") },
                    modifier = Modifier.menuAnchor()
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

/* 旧バージョン
@Composable
fun TimeDropdownPicker(
    label: String,
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {

    val hours = (0..23).toList()
    val minutes = listOf(0, 30)

    var expanded by remember { mutableStateOf(false) }

    var selectedHour by remember { mutableStateOf(selectedTime.hour) }
    var selectedMinute by remember { mutableStateOf(selectedTime.minute) }

    Column {

        Text(label)

        Row {

            // ===== 時 =====
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {

                OutlinedTextField(
                    value = selectedHour.toString().padStart(2, '0'),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("時") },
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .width(100.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    hours.forEach { hour ->
                        DropdownMenuItem(
                            text = {
                                Text(hour.toString().padStart(2, '0'))
                            },
                            onClick = {
                                selectedHour = hour
                                onTimeSelected(
                                    LocalTime.of(
                                        selectedHour,
                                        selectedMinute
                                    )
                                )
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // ===== 分 =====
            var minuteExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = minuteExpanded,
                onExpandedChange = { minuteExpanded = it }
            ) {

                OutlinedTextField(
                    value = selectedMinute.toString().padStart(2, '0'),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("分") },
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .width(100.dp)
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
                                onTimeSelected(
                                    LocalTime.of(
                                        selectedHour,
                                        selectedMinute
                                    )
                                )
                                minuteExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
 */