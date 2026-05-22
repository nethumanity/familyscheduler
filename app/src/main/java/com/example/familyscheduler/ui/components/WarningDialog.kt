package com.example.familyscheduler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.presentation.renderMissingReason

@Composable
fun WarningDialog(
    index: Int,
    evaluation: AvailabilityEvaluation,
    initialPage: Int,
    onDismiss: () -> Unit,
    onApplyProposal: (FlexResolveProposal) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { evaluation.reasons.size ?: 0 }
    )
    val currentReason = evaluation.reasons.getOrNull(pagerState.currentPage)
    val hasProposal = currentReason?.proposals?.isNotEmpty() == true

    var selectedProposal by remember(pagerState.currentPage) {
        mutableStateOf<FlexResolveProposal?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        },
        dismissButton = {
            if (hasProposal) {
                TextButton(
                    enabled = selectedProposal != null,
                    onClick = {
                        selectedProposal?.let { onApplyProposal(it) }
                    }
                ) {
                    Text("この提案を実行")
                }
            }
        },
        title = {
            Text("${TimeAxis.all[index]} の予定を確認してください")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                if (evaluation != null) {

                    HorizontalPager(state = pagerState) { page ->

                        val reason = evaluation.reasons[page]

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(renderMissingReason(reason.reason))

                            if (reason.proposals.isNotEmpty()) {
                                HorizontalDivider()
                                Text("解消案", fontWeight = FontWeight.Bold)

                                reason.proposals.forEach { proposal ->
                                    ProposalRow(
                                        proposal = proposal,
                                        selected = selectedProposal == proposal,
                                        onSelect = { selectedProposal = proposal }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(evaluation.reasons.size) { i ->
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(6.dp)
                                    .background(
                                        if (pagerState.currentPage == i) Color.DarkGray else Color.LightGray,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    )
}
