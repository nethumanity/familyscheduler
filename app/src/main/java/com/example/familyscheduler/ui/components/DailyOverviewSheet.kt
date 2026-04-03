package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.evaluation.AvailabilityState
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.utilities.renderBlockingPersons
import com.example.familyscheduler.ui.utilities.renderMissingReasonCount
import com.example.familyscheduler.ui.utilities.slotStateLabel
import com.example.familyscheduler.ui.utilities.toUiModels
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun DailyOverviewSheet(
    viewModel: TimelineViewModel,
    onToggle: () -> Unit,
    onEditRequirement: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var menuPosition by remember { mutableStateOf<Offset?>(null) }
    var expandedMenuId by remember { mutableStateOf<String?>(null) }

    fun indexToTime(index: Int): String {
        return TimeAxis.all.getOrNull(index)?.toString() ?: "--:--"
    }
    fun List<AvailabilityEvaluation>.extractWarnings() =
        filter { it.state == AvailabilityState.WARN }

    val warnings = uiState.evaluations.extractWarnings()
    val warningMap = warnings
        .flatMap { it.reasons }
        .associateBy { it.sourceRuleId }
    val uiRequirements = uiState.rules
        .toUiModels(
            requirements = uiState.requirements,
            overrides = uiState.overrides,
            viewModel = viewModel
        )
        .filter { it.name.isNotEmpty() }
        .sortedBy { it.startIndex }

    LazyColumn(
        //modifier = Modifier.fillMaxHeight(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("📅 ${uiState.date}", fontWeight = FontWeight.Bold)

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

                    val blockText = renderBlockingPersons(reason)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onAvailabilityWarningClick(
                                index = eval.index,
                                reasonIndex = i
                                )
                            } //onWarningClick(eval.index, i) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            indexToTime(eval.index),
                            modifier = Modifier.width(60.dp)
                        )

                        val nameText = if (reason.requirementName == "") {
                            slotStateLabel(
                                uiState.rules.first { it.id == reason.sourceRuleId }.targetState
                            )
                        } else {
                            reason.requirementName
                        }

                        Text(nameText)
                        Spacer(Modifier.weight(1f))
                        Text("${blockText}　提案：${eval.flexProposals.size}")
                    }
                }
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
            val count = reason?.let { renderMissingReasonCount(it) }
            val assignedPersons = viewModel.getAssignedPersons(req.id)
                .joinToString(" ") { it.label }

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
                    onToggle()
                },
                onLongClick = {
                    expandedMenuId = req.id
                }
            )

            DropdownMenu(
                expanded = expandedMenuId == req.id,
                onDismissRequest = { expandedMenuId = null }
            ) {
                DropdownMenuItem(
                    text = { Text("編集") },
                    onClick = {
                        expandedMenuId = null
                        onEditRequirement(req.id)
                    }
                )
                DropdownMenuItem(
                    text = { Text("削除") },
                    onClick = {
                        expandedMenuId = null
                        viewModel.deleteRequirement(req.id)
                    }
                )
            }
        }
    }
}