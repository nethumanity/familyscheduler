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
import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.ui.presentation.renderMissingReason
import com.example.familyscheduler.ui.projection.WarningUiModel
import java.time.LocalTime

@Composable
fun WarningDialog(
    time: LocalTime,
    warningPages: List<WarningUiModel>,
    proposalsById: Map<String, List<FlexResolveProposal>>,
    initialPage: Int,
    onDismiss: () -> Unit,
    onApplySolo: (WarningUiModel) -> Unit,
    onApplyCanceled: (WarningUiModel) -> Unit,
    onApplyProposal: (FlexResolveProposal) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { warningPages.size }
    )

    val current =
        warningPages.getOrNull(pagerState.currentPage)

    val currentProposals =
        current?.let {
            proposalsById[it.dialogKey.ruleId]
        }.orEmpty()

    var selectedProposal by remember(pagerState.currentPage) {
        mutableStateOf<FlexResolveProposal?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (currentProposals.isNotEmpty()) {
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
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        },
        title = {
            Text("${time} の予定を確認してください")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                HorizontalPager(state = pagerState) { page ->

                    val warning = warningPages[page]

                    val proposals =
                        proposalsById[warning.dialogKey.ruleId].orEmpty()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(renderMissingReason(warning))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (warning.cancelApplicable == true) {
                                TextButton(
                                    onClick = { onApplyCanceled(warning) }
                                ) {
                                    Text("予定をキャンセル")
                                }
                            }
                            Box(
                                modifier = Modifier.weight(1f)
                            ) { }
                            if (warning.soloApplicable == true) {
                                TextButton(
                                    onClick = { onApplySolo(warning) }
                                ) {
                                    Text("${warning.personStates.assignablePersons.single().label} 1人で対応")
                                }
                            }
                        }

                        if (proposals.isNotEmpty()) {
                            HorizontalDivider()
                            Text("解消案", fontWeight = FontWeight.Bold)

                            proposals.forEach { proposal ->
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
                    repeat(warningPages.size) { i ->
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
    )
}
