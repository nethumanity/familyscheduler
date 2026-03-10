@file: OptIn(ExperimentalMaterial3Api::class)

package com.example.familyscheduler.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.familyscheduler.ui.inputs.ChildRoutineInputScreen
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel

@Composable
fun ChildScreen(
    viewModel: ChildRoutineViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {

    var page by remember { mutableStateOf(ChildPage.LIST) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onBack,
        modifier = Modifier.fillMaxHeight(0.95f)    // いずれ削除？
    ) {

        when (page) {

            ChildPage.LIST -> {
                ChildListSheet(
                    onAddClick = {
                        page = ChildPage.INPUT
                    }
                )
            }

            ChildPage.INPUT -> {
                ChildRoutineInputScreen(
                    viewModel = viewModel,
                    onBack = {
                        page = ChildPage.LIST
                    },
                    onSaved = {
                        onSaved()
                    }
                )
            }
        }
    }
}