@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)

package com.example.familyscheduler.ui.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.R
import com.example.familyscheduler.domain.evaluation.AvailabilityState
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.components.SlotStateSelectionSheet
import com.example.familyscheduler.ui.components.WarningDialog
import com.example.familyscheduler.ui.utilities.slotStateColor
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    persons: List<Person> =
        listOf(Person.FATHER, Person.MOTHER),
    onOpenTemplateSheet: (Person) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.warningDialogState.collectAsState()

    val ruleMap = remember(uiState.rules) { uiState.rules.associateBy { it.id } }
    val slotMap = remember(uiState.slots) { uiState.slots.groupBy { it.index } }

    var editingSlot by remember { mutableStateOf<Pair<Int, Person>?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var totalDrag = 0f

                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (totalDrag > 200) {
                                viewModel.changeDate(uiState.date.minusDays(1))
                            } else if (totalDrag < -200) {
                                viewModel.changeDate(uiState.date.plusDays(1))
                            }
                            totalDrag = 0f
                        }
                    ) { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                }
        ) {
            stickyHeader {
                TimelineHeaderRow(
                    viewModel = viewModel,
                    persons = persons,
                    dailyStates = uiState.dailyStates,
                    onDailyStateClick = { person ->
                        onOpenTemplateSheet(person)
                    }
                )
            }

            items(
                count = TimeAxis.displayEndIndex - TimeAxis.displayStartIndex + 1
            ) { offset ->

                val index = TimeAxis.displayStartIndex + offset

                val time = TimeAxis.all[index]
                val rowSlots = slotMap[index] ?: emptyList()//uiState.slots.filter { it.index == index }
                val rowSlotMap = remember(rowSlots) { rowSlots.associateBy { it.person } }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(0.5.dp, Color.LightGray)
                        .pointerInput(persons, time) {

                            detectTapGestures(
                                onTap = {
                                    val evaluation = uiState.evaluationsByIndex[index]

                                    if (evaluation?.state == AvailabilityState.WARN) {
                                        viewModel.onAvailabilityWarningClick(index, 0)
                                    }
                                },
                                onLongPress = { offset ->
                                    val timeColumnWidthPx = 64.dp.toPx()

                                    if (offset.x < timeColumnWidthPx)
                                        return@detectTapGestures

                                    val columnWidth =
                                        (size.width - timeColumnWidthPx) / persons.size

                                    val columnIndex =
                                        ((offset.x - timeColumnWidthPx) / columnWidth)
                                            .toInt()
                                            .coerceIn(0, persons.lastIndex)

                                    val person = persons[columnIndex]

                                    editingSlot = index to person
                                }
                            )
                        }
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

                        val evaluation = uiState.evaluationsByIndex[index]

                        if (evaluation?.state == AvailabilityState.WARN) {
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
                        val slot = rowSlotMap[person]
                        val taskNames = remember(slot, ruleMap) {
                            slot?.taskIds?.mapNotNull { ruleMap[it]?.taskName }
                                ?: emptyList()
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
                                text = taskNames
                                    .joinToString("  "),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        TimelineOverlay(
            settings = uiState.settings,
            slots = uiState.slots,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
                .offset(x = 32.dp)
        )
    }

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

    dialogState?.let { state ->

        val evaluation = uiState.evaluationsByIndex[state.index]
            ?: return@let

        WarningDialog(
            index = state.index,
            evaluation = evaluation,
            initialPage = state.reasonIndex,
            onDismiss = {
                viewModel.dismissWarningDialog()
            },
            onApplyProposal = {
                viewModel.applyFlexResolveProposal(it)
            }
        )
    }
}
