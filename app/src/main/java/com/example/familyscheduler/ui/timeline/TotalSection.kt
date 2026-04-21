package com.example.familyscheduler.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.utilities.slotStateColor

@Composable
fun TotalSection(slots: List<TimeSlot>) {

    val grouped = remember(slots) { slots.groupBy { it.person to it.state } }

    val stepMinutes = TimeAxis.stepMinutes

    fun formatHours(hours: Double): String {
        return String.format("%.1f時間", hours)
    }

    Column {
        //Text("集計", fontSize = 10.sp, fontWeight = FontWeight.Bold)

        SlotState.totalSectionAllowedState
            .forEach { state ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fatherSlots =
                        grouped[Person.FATHER to state]?.size ?: 0
                    val motherSlots =
                        grouped[Person.MOTHER to state]?.size ?: 0
                    val fatherHours = fatherSlots * stepMinutes / 60.0
                    val motherHours = motherSlots * stepMinutes / 60.0

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slotStateColor(state))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(formatHours(fatherHours), fontSize = 11.sp)
                    Spacer(Modifier.width(120.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slotStateColor(state))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(formatHours(motherHours), fontSize = 11.sp)
                }
            }
    }
}
