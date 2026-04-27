package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.ui.utilities.DayOfWeekUtilities
import java.time.DayOfWeek

@Composable
fun DaysOfWeekSection(
    everyDay: Boolean,
    selectedDays: Set<DayOfWeek>,
    onToggleEveryDay: (Boolean) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit
) {
    Column {

        Text("曜日")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = everyDay,
                onCheckedChange = onToggleEveryDay
            )

            Text("毎日の予定")
        }

        if (!everyDay) {

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                DayOfWeek.entries.forEach { day ->

                    val color =
                        when(day) {
                            DayOfWeek.SATURDAY -> Color.Blue
                            DayOfWeek.SUNDAY -> Color.Red
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                    val selected =
                        day in selectedDays

                    FilterChip(
                        selected = selected,
                        onClick = { onToggleDay(day) },
                        label = {
                            Text(text = DayOfWeekUtilities.short(day), color = color)
                        }
                    )
                }
            }
        }
    }
}