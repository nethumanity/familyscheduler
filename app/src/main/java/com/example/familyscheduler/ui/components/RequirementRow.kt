package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClearProposal: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val baseColor = when {
        req.mode == RequirementModeToday.CANCELED ->  Color.LightGray
        req.isProposalApplied && req.mode != RequirementModeToday.CANCELED -> Color(0xFF1976D2)
        else -> Color.Unspecified
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                TimeAxis.all.getOrNull(req.startIndex)?.toString() ?: "--:--",
                modifier = Modifier.width(60.dp),
                color = baseColor
            )

            Text(
                text = req.name,
                color = baseColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

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
                        Text("➥ $assignedPersons", color = Color.Cyan)
                    }
                }

                RequirementModeToday.CANCELED -> {
                    Text("キャンセル", color = Color.LightGray)
                }
            }
        }

        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (req.canEdit || req.isProposalApplied) {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.matchParentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "menu",
                        tint = Color.LightGray
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (req.canEdit) {
                        DropdownMenuItem(
                            text = { Text("編集") },
                            onClick = {
                                expanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("削除") },
                            onClick = {
                                expanded = false
                                onDelete()
                            }
                        )
                    }
                    if (req.isProposalApplied) {
                        DropdownMenuItem(
                            text = { Text("提案の実行を取り消す") },
                            onClick = {
                                expanded = false
                                onClearProposal()
                            }
                        )
                    }
                }
            }
        }
    }
}