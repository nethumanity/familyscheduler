@file: OptIn(ExperimentalMaterial3Api::class)

package com.example.familyscheduler.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.familyscheduler.ui.inputs.ChildRoutineInputScreen
import com.example.familyscheduler.ui.manager.ChildPage
import com.example.familyscheduler.ui.utilities.editingTarget
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel
import java.time.LocalDate

@Composable
fun ChildScreen(
    viewModel: ChildRoutineViewModel,
    currentDate: LocalDate,
    onClose: () -> Unit,
    onToggle: () -> Unit,
    onDeleteChildRoutine: () -> Unit
) {

    var page by remember { mutableStateOf(ChildPage.LIST) }

    when (page) {

        ChildPage.LIST -> {
            ChildListSheet(
                viewModel = viewModel,
                currentDate = currentDate,
                onAddClick = {
                    page = ChildPage.INPUT
                },
                onToggle = onToggle,
                onEditChildRoutine = { childName ->
                    viewModel.startEditChildRoutine(childName)
                    page = ChildPage.INPUT
                },
                onDeleteChildRoutine = onDeleteChildRoutine
            )
        }

        ChildPage.INPUT -> {
            val target = editingTarget

            LaunchedEffect(target?.childRoutineId) {
                val id = target?.childRoutineId ?: return@LaunchedEffect

                viewModel.load(id)
                viewModel.clearEditingTarget()
            }

            ChildRoutineInputScreen(
                viewModel = viewModel,
                onBack = {
                    page = ChildPage.LIST
                },
                onSaved = {
                    onClose()
                }
            )
        }
    }
}
