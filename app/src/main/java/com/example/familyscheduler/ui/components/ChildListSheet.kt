package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel
import java.time.LocalDate

@Composable
fun ChildListSheet(
    viewModel: ChildRoutineViewModel,
    currentDate: LocalDate,
    onAddClick: () -> Unit,
    onEditChildRoutine: (String) -> Unit
) {
    // リファクタ候補：MainScreenからchildren, overridesを引数で受け取る
    val uiState by viewModel.uiState.collectAsState()

    val children = uiState.routines
    val overrides = uiState.overrides

    var expandedMenuId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "子ども",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onAddClick) {
                Text("登録")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {

            items(children) { child ->

                val todayRoutine = viewModel.resolveTodayRoutine(
                    child,
                    currentDate,
                    overrides)

                ChildRow(
                    child = child,
                    routine = todayRoutine,
                    onToggle = {
                        viewModel.toggleTodayRoutine(child, currentDate)
                    },
                    onMenuClick = { expandedMenuId = child.name }
                )

                DropdownMenu(
                    expanded = expandedMenuId == child.name,
                    onDismissRequest = { expandedMenuId = null }
                ) {
                    DropdownMenuItem(
                        text = { Text("編集") },
                        onClick = {
                            expandedMenuId = null
                            onEditChildRoutine(child.name)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("削除") },
                        onClick = {
                            expandedMenuId = null
                            viewModel.deleteChildRoutine(child.name)
                        }
                    )
                }
            }
        }
    }
}