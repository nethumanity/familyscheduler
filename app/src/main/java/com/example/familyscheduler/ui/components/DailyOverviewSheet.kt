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
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.evaluation.AvailabilityState
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.mapper.toUiModels
import com.example.familyscheduler.ui.presentation.SlotStatePresentation
import com.example.familyscheduler.ui.presentation.renderBlockingPersons
import com.example.familyscheduler.ui.presentation.renderMissingReasonCount
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun DailyOverviewSheet(
    viewModel: TimelineViewModel,
    onEditRequirement: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    fun indexToTime(index: Int): String {
        return TimeAxis.all.getOrNull(index)?.toString() ?: "--:--"
    }
    fun List<AvailabilityEvaluation>.extractWarnings() =
        filter { it.state == AvailabilityState.WARN }

    val warnings = uiState.evaluations.extractWarnings()
    val warningMap = warnings
        .flatMap { it.reasons }
        .associateBy { it.reason.sourceRuleId }
    val uiRequirements = uiState.rules
        .toUiModels(
            requirements = uiState.requirements,
            overrides = uiState.overrides,
            shiftOverrides = uiState.routineShiftOverrides,
            events = uiState.childCareEvents,
            date = uiState.date,
            viewModel = viewModel
        )
        .filter { it.name.isNotEmpty() }
        .sortedBy { it.startIndex }

    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("${uiState.date}", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        }
        item {
            Spacer(Modifier.height(8.dp))
        }

        // =========================
        // 警告一覧
        // =========================
        if (warnings.isNotEmpty()) {

            item {
                Text("⚠ 警告", fontWeight = FontWeight.Bold)
            }

            items(warnings) { eval ->
                eval.reasons.forEachIndexed { i, reason ->

                    val blockText = renderBlockingPersons(reason.reason)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onAvailabilityWarningClick(
                                index = eval.index,
                                reasonIndex = i
                                )
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            indexToTime(eval.index),
                            modifier = Modifier.width(60.dp)
                        )

                        val nameText = if (reason.reason.requirementName == "") {
                            SlotStatePresentation.label(
                                uiState.rules.first { it.id == reason.reason.sourceRuleId }.targetState
                            )
                        } else {
                            reason.reason.requirementName
                        }

                        Text(
                            text = nameText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(blockText)

                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (reason.proposals.isNotEmpty()) {
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
        }

        // =========================
        // 育児担当切替
        // =========================
        if (uiState.reverseBlocks.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("👥 育児担当切替", fontWeight = FontWeight.Bold)
            }
            items(uiState.reverseBlocks) { block ->

                ReverseAssignableBlockRow(
                    block = block,
                    onReverse = { viewModel.toggleReverse(block) }
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
        items(uiRequirements
                .filter { it.name != "" }.sortedBy { it.startIndex }
        ) { req ->

            val reason = warningMap[req.id]
            val isWarn = reason != null
            val count = reason?.let { renderMissingReasonCount(it.reason) }
            val assignedPersons = viewModel.getAssignedPersons(req.id)
                .joinToString(" ") { it.label }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                RequirementRow(
                    req = req,
                    isWarn = isWarn,
                    count = count,
                    assignedPersons = assignedPersons,
                    onClick = {
                        viewModel.toggleRequirementMode(
                            uiState.rules.first { it.id == req.id },
                            uiState.requirements
                                .filterIsInstance<TimeRangeHouseholdRequirement>()
                                .firstOrNull { it.sourceRuleId == req.id }
                        )
                    },
                    onEdit = { onEditRequirement(req.id) },
                    onDelete = { viewModel.deleteRequirement(req.id) },
                    onClearProposal = { viewModel.clearProposal(req.id) }
                )
            }
        }
    }
}