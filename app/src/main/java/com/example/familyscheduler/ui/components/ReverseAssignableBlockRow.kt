package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.interaction.ReverseAssignableBlock
import com.example.familyscheduler.domain.time.TimeAxis

@Composable
fun ReverseAssignableBlockRow(
    block: ReverseAssignableBlock,
    onReverse: () -> Unit
) {
    fun indexToTime(index: Int): String {
        return TimeAxis.all.getOrNull(index)?.toString() ?: "--:--"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReverse() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${indexToTime(block.startIndex)}–" +
                        indexToTime(block.endIndex),
                modifier = Modifier.weight(1f)
            )

            Text(
                "✔ ${block.assignedPerson.label} ↔ ${block.reversedPerson.label}"
            )

            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {}
        }
    }
}