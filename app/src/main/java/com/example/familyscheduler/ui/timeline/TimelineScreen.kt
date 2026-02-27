@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)

package com.example.familyscheduler.ui.timeline

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.R
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.components.SlotStateSelectionSheet
import com.example.familyscheduler.ui.components.slotStateColor
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    persons: List<Person> =
        listOf(Person.FATHER, Person.MOTHER)
    //viewModel_toBeRemoved: MainViewModel
) {

    //val persons = listOf(Person.FATHER, Person.MOTHER)

    val currentDate by viewModel.currentDate.collectAsState()
    val dailyStates by viewModel.dailyStates.collectAsState()
    val slots by viewModel.slots.collectAsState()
    val evaluations by viewModel.evaluations.collectAsState()

    LaunchedEffect(currentDate) {
        Log.d("TimelineScreen", "reload triggered")
        viewModel.loadForDate(currentDate)
    }

    LaunchedEffect(slots){
        Log.d("TimelineScreen", "slots received = ${slots.size}")
    }

    var editingSlot by remember { mutableStateOf<Pair<Int, Person>?>(null) }
    //var showAddDailyStateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        stickyHeader {
            TimelineHeaderRow(
                persons = persons,
                dailyStates = dailyStates,
                onDailyStateClick = { person ->
                    viewModel.onTemplateHeaderClick(person)
                }
            )
        }

        items(
            count = TimeAxis.displayEndIndex - TimeAxis.displayStartIndex + 1
        ) { offset ->

            val index = TimeAxis.displayStartIndex + offset

            val time = TimeAxis.all[index]
            val rowSlots = slots.filter { it.index == index }   //あるいは、viewModel.slotsAt(index)

            Log.d(
                "TimelineScreen",
                "index=$index rowSlots=${rowSlots.map{it.person}}"
            )
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

                    //★編集中
                    val evaluation = evaluations.getOrNull(index)
                    if (evaluation?.missing ?: 0 > 0) {
                    //if (availabilityState.shouldWarn) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_warning),
                            contentDescription = "warning",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 各personのslot
                persons.forEach { person ->
                    val slot = rowSlots.firstOrNull { slot ->
                        slot.person.name == person.name
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
                    viewModel.changeSlotState(
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
    // DailyTemplate変更シート
    // ============================

    viewModel.editingTemplateFor?.let { person ->

        ModalBottomSheet(
            onDismissRequest = {
                viewModel.dismissTemplateSheet()
            }
        ) {

            Text(
                text = "${person.label} のテンプレート",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn {

                items(viewModel.templates) { template ->

                    ListItem(
                        headlineContent = {
                            Text(
                                text = template.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.clickable {
                            viewModel.applyTemplate(person, template)
                        }
                    )
                }
            }
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

}
