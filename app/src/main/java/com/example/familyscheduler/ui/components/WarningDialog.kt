package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.utilities.renderMissingReason

@Composable
fun WarningDialog(
    index: Int,
    evaluation: AvailabilityEvaluation,
    flexProposals: List<FlexResolveProposal>,
    onDismiss: () -> Unit,
    onApplyProposal: (FlexResolveProposal) -> Unit
) {
    var selectedProposal by remember {
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
            if (flexProposals.isNotEmpty()) {
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

                evaluation.reasons.forEach { reason ->
                    Text(renderMissingReason(reason))
                }

                if (flexProposals.isNotEmpty()) {
                    HorizontalDivider()

                    Text("解消案", fontWeight = FontWeight.Bold)

                    flexProposals.forEach { proposal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedProposal = proposal }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedProposal == proposal,
                                onClick = { selectedProposal = proposal }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text =
                                    "${proposal.person} の " +
                                            "${proposal.requirementName} 予定を " +
                                            "${proposal.deltaMinutes}分ずらす",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    )
}
