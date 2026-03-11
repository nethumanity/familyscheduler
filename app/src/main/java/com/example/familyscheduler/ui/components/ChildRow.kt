package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.requirement.ChildRoutineInput
import com.example.familyscheduler.domain.requirement.ChildTodayRoutine

@Composable
fun ChildRow(
    child: ChildRoutineInput,
    routine: ChildTodayRoutine,
    onToggle: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = child.name,
            fontSize = 16.sp
        )

        Text(
            text = when(routine) {
                ChildTodayRoutine.NURSERY -> "登園"
                ChildTodayRoutine.HOME -> "在宅"
                ChildTodayRoutine.NONE -> "保育なし"
            },
            color = Color.Gray
        )
    }
}