package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.StateCategory

@Composable
fun FixedScheduleSection(
    schedules: MutableList<ScheduleTemplate>
) {

    Column {

        Text("仕事")

        var enabled by remember { mutableStateOf(true) }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = !enabled,
                onCheckedChange = {
                    enabled = !it
                }
            )

            Text("仕事なし")
        }



        if (enabled) {

            TimeRangeEditor(
                StateCategory.WORK,
                "仕事",
                schedules
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("往路通勤")

        TimeRangeEditor(
            StateCategory.BLOCKED,
            "往路通勤",
            schedules
        )

        Spacer(Modifier.height(16.dp))

        Text("復路通勤")

        TimeRangeEditor(
            StateCategory.BLOCKED,
            "復路通勤",
            schedules
        )

        Spacer(Modifier.height(16.dp))

        Text("睡眠")

        TimeRangeEditor(
            StateCategory.REST,
            "睡眠",
            schedules
        )
    }
}