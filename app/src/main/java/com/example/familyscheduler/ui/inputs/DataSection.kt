@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.viewmodel.OneTimeAppointmentViewModel
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun DateSection(
    state: OneTimeAppointmentViewModel.OneTimeAppointmentInput,
    viewModel: OneTimeAppointmentViewModel
) {
    var showDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.date
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
    )

    Column {

        Text("日付")

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showDialog = true }
        ) {
            Text(
                state.date?.format(DateTimeFormatter.ISO_DATE)
                    ?: "日付を選択"
            )
        }

        if (showDialog) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                viewModel.updateDate(selectedDate)
                            }
                            showDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("キャンセル")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}