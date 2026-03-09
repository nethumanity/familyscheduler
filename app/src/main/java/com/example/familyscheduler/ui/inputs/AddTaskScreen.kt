package com.example.familyscheduler.ui.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.familyscheduler.viewmodel.OneTimeTaskViewModel
import com.example.familyscheduler.viewmodel.WeeklyTaskViewModel

@Composable
fun AddTaskScreen(
    oneTimeViewModel: OneTimeTaskViewModel,
    weeklyViewModel: WeeklyTaskViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {

    var selectedTab by remember { mutableStateOf(0) }

    Column {

        TabRow(selectedTabIndex = selectedTab) {

            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("日付指定") }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("毎週（毎日）") }
            )
        }

        when (selectedTab) {

            0 -> OneTimeTaskInputScreen(oneTimeViewModel, onBack, onSaved)

            1 -> WeeklyTaskInputScreen(weeklyViewModel, onBack, onSaved)
        }
    }
}