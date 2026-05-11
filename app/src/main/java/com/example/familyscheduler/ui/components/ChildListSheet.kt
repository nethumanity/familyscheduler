package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val uiState by viewModel.uiState.collectAsState()

    val children = uiState.routines
    val overrides = uiState.overrides

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

                Box {
                    ChildRow(
                        child = child,
                        routine = todayRoutine,
                        onToggle = { viewModel.toggleTodayRoutine(child, currentDate) },
                        onEdit = { onEditChildRoutine(child.childId) },
                        onDelete = { viewModel.deleteChildRoutine(child.childId) }
                    )
                }
            }
        }
    }
}