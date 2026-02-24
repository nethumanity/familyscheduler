@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)

package com.example.familyscheduler.ui.timeline

import android.R.attr.padding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familyscheduler.R
import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeAxis.indexOf
import com.example.familyscheduler.ui.components.SlotStateSelectionSheet
import com.example.familyscheduler.ui.components.renderMissingReason
import com.example.familyscheduler.ui.components.slotStateColor
import com.example.familyscheduler.viewmodel.MainViewModel
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    viewModel_toBeRemoved: MainViewModel
) {

    val persons = listOf(Person.FATHER, Person.MOTHER)

    val currentDate by viewModel.currentDate.collectAsState()
    val dailyStates by viewModel.dailyStates.collectAsState()
    val slots by viewModel.slots.collectAsState()

    var editingSlot by remember { mutableStateOf<Pair<Int, Person>?>(null) }
    var showAddDailyStateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        stickyHeader {
            TimelineHeaderRow(
                persons = persons,
                dailyStates = dailyStates,
                onDailyStateClick = {
                    //viewModel.onDailyStateClick(it)
                }
            )
        }

        items(TimeAxis.indices.toList()) { index ->

            val time = TimeAxis.all[index]
            val rowSlots = viewModel.slotsAt(index)
            //val availabilityState =
            //  viewModel.availabilityStateAt(time)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(0.5.dp, Color.LightGray)
                /*.pointerInput(persons, time) {
                    detectTapGestures(
                        onTap = {
                            if (availabilityState.shouldWarn) {
                                viewModel.onAvailabilityWarningClick(indexOf(time))
                            }
                        },
                        onLongPress = { offset ->
                            val columnWidth =
                                (size.width - 64.dp.toPx()) / persons.size

                            val index =
                                ((offset.x - 64.dp.toPx()) / columnWidth).toInt()

                            val person = persons.getOrNull(index)
                            if (person != null) {
                                editingSlot = time to person
                            }
                        }
                    )
                }

                 */
            ) {

                // 時刻表示
                Column(
                    modifier = Modifier.width(64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = time.toString(),
                        fontSize = 12.sp
                    )

                    /*if (availabilityState.shouldWarn) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_warning),
                            contentDescription = "warning",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                     */
                }

                // 各personのslot
                persons.forEach { person ->
                    val slot = rowSlots.find {
                        it.person == person
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                slot?.let {
                                    slotStateColor(it.state)
                                } ?: Color.LightGray
                            )
                            .border(0.5.dp, Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            slot?.taskName ?: "",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // ============================
    // Slot編集シート
    // ============================

    editingSlot?.let { (index, person) ->

        val time = TimeAxis.all[index]

        ModalBottomSheet(
            onDismissRequest = {
                editingSlot = null
            }
        ) {
            SlotStateSelectionSheet(
                time = time,
                person = person,
                onSelect = { newState ->
                    viewModel_toBeRemoved.changeSlotState(
                        index = index,
                        person = person,
                        newState = newState
                    )
                    editingSlot = null
                }
            )
        }
    }

    // ============================
    // ③　警告ダイアログ　消された！！！
    // ============================
    /*
    val dialogIndex = viewModel.warningDialogIndex

    dialogIndex?.let { index ->
        val evaluation = viewModel.evaluations.value[index]
        val flexProposals = viewModel.flexResolveProposalsAt(index)
        var selectedProposal by remember(dialogIndex) {
            mutableStateOf<FlexResolveProposal?>(null)
        }

        AlertDialog(
            onDismissRequest = {
                viewModel.dismissWarningDialog()
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissWarningDialog()
                }) {
                    Text("閉じる")
                }
            },
            dismissButton = {
                if (flexProposals.isNotEmpty()) {
                    TextButton(
                        enabled = selectedProposal != null,
                        onClick = {
                            selectedProposal?.let {
                                viewModel.applyFlexResolveProposal(it)
                            }
                        }
                    ) {
                        Text("この提案を実行")
                    }
                }
            },
            title = {
                Text("${TimeAxis.all[dialogIndex]} の予定に問題があります")
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

     */

    // ============================
    // 初期ロード
    // ============================

    LaunchedEffect(currentDate) {
        viewModel.loadForDate(currentDate)
    }
}
