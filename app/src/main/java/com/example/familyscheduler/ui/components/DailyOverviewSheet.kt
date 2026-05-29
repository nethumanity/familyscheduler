package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.R
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun DailyOverviewSheet(
    viewModel: TimelineViewModel,
    onEditRequirement: (String) -> Unit
) {
    val dailyOverviewUiState by viewModel.dailyOverviewUiState.collectAsState()
    val warningItems = dailyOverviewUiState.warningItems
    val careStateItems = dailyOverviewUiState.careStateItems
    val requirementItems = dailyOverviewUiState.requirementItems

    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("${dailyOverviewUiState.date}", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        }
        item {
            Spacer(Modifier.height(8.dp))
        }

        // =========================
        // 警告一覧
        // =========================
        if (warningItems.isNotEmpty()) {

            item {
                Text("⚠ 警告", fontWeight = FontWeight.Bold)
            }

            items(warningItems) { item ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.openWarningDialog(item.dialogKey)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.timeText,
                        modifier = Modifier.width(110.dp)
                    )

                    Text(
                        text = item.nameText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.cancelApplicable) {
                            Text("\uD83D\uDEAB")
                        }
                    }

                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.soloApplicable) {
                            Text("\uD83D\uDC64")
                        }
                    }

                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.hasProposal) {
                            Icon(
                                painter = painterResource(R.drawable.ic_proposal),
                                contentDescription = "Proposal",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }
            }
        }

        // =========================
        // 育児担当切替
        // =========================
        if (careStateItems.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("👥 育児担当", fontWeight = FontWeight.Bold)
            }
            items(careStateItems) { item ->

                CareStateBlockRow(
                    item = item,
                    onToggle = { viewModel.toggleCareStateMode(item) }
                )
            }
        }

        // =========================
        // 予定一覧
        // =========================
        item {
            Spacer(Modifier.height(8.dp))
            Text("■ 予定一覧", fontWeight = FontWeight.Bold)
        }
        items(requirementItems) { item ->

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                RequirementRow(
                    item = item,
                    onToggle = { viewModel.toggleRequirementMode(item) },
                    onEdit = { onEditRequirement(item.requirementId) },
                    onDelete = { viewModel.deleteRequirement(item.requirementId) },
                    onClearProposal = { viewModel.clearProposal(item.requirementId) }
                )
            }
        }
    }
}