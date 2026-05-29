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
import com.example.familyscheduler.ui.presentation.StateTextPresentation.baseColor
import com.example.familyscheduler.ui.presentation.StateTextPresentation.stateColor
import com.example.familyscheduler.ui.presentation.StateTextPresentation.stateText
import com.example.familyscheduler.ui.projection.CareStateUiModel

@Composable
fun CareStateBlockRow(
    item: CareStateUiModel,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
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
                text = item.timeText,
                color = baseColor(item.mode),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = stateText(item.status),
                color = stateColor(item.status)
            )

            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) { }
        }
    }
}