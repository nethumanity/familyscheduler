package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.utilities.RequirementUiModel

@Composable
fun RequirementRow(
    req: RequirementUiModel,
    isWarn: Boolean,
    count: String?,
    assignedPersons: String,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val baseColor =
        if (req.mode == RequirementModeToday.CANCELED) Color.LightGray
        else Color.Unspecified

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), //2 → 4
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
                .padding(end = 8.dp), // ← ケバブとの距離
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                TimeAxis.all.getOrNull(req.startIndex)?.toString() ?: "--:--",
                modifier = Modifier.width(60.dp),
                color = baseColor
            )

            Text(req.name, color = baseColor)

            Spacer(Modifier.weight(1f))

            when (req.mode) {
                RequirementModeToday.AUTO -> {
                    if (!isWarn) {
                        Text("✔ $assignedPersons", color = Color.Green)
                    } else {
                        Text("⚠ $count", color = Color.Red)
                    }
                }

                RequirementModeToday.REVERSE -> {
                    if (!isWarn) {
                        Text("➥ $assignedPersons", color = Color.Blue)
                    }
                }

                RequirementModeToday.CANCELED -> {
                    Text("キャンセル", color = Color.LightGray)
                }
            }
        }

        Box(
            modifier = Modifier.size(32.dp), // ← IconButtonと同じサイズ
            contentAlignment = Alignment.Center
        ) {
            if (req.canEdit) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.matchParentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "menu",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}