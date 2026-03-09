package com.example.familyscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.familyscheduler.data.repository.InMemoryHouseholdRequirementRepository
import com.example.familyscheduler.ui.components.SettingsScreen
import com.example.familyscheduler.ui.inputs.AddTaskScreen
import com.example.familyscheduler.ui.inputs.ScheduleInputScreen
import com.example.familyscheduler.ui.theme.FamilySchedulerTheme
import com.example.familyscheduler.ui.timeline.FooterBar
import com.example.familyscheduler.ui.timeline.HeaderBar
import com.example.familyscheduler.ui.timeline.TimelineScreen
import com.example.familyscheduler.viewmodel.Factory.OneTimeTaskViewModelFactory
import com.example.familyscheduler.viewmodel.Factory.TimelineViewModelFactory
import com.example.familyscheduler.viewmodel.Factory.WeeklyTaskViewModelFactory
import com.example.familyscheduler.viewmodel.OneTimeTaskViewModel
import com.example.familyscheduler.viewmodel.TimelineViewModel
import com.example.familyscheduler.viewmodel.WeeklyTaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilySchedulerTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val repository = remember{ InMemoryHouseholdRequirementRepository() }

    val timelineViewModel: TimelineViewModel =
        viewModel(factory = TimelineViewModelFactory(repository))



    Scaffold(
        topBar = {

            when (currentRoute) {

                "timeline" -> {

                    HeaderBar(
                        date = timelineViewModel.currentDate.collectAsState().value,
                        onPreviousDay = {
                            timelineViewModel.moveToPreviousDay()
                        },
                        onNextDay = {
                            timelineViewModel.moveToNextDay()
                        }
                    )
                }

                else -> {
                    // 設定画面ではTopBarなし
                }
            }
        },

        bottomBar = {
            FooterBar(
                // 今後ここに追加していく（route = calender, children, today）
                onAddClick = {
                    navController.navigate("add_task")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "timeline",
            modifier = Modifier.padding(padding)
        ) {

            composable("timeline") {
                TimelineScreen(
                    viewModel = timelineViewModel
                )
            }

            //composable() 今後ここに追加していく

            composable("add_task") {

                val oneTimeViewModel: OneTimeTaskViewModel =
                    viewModel(
                        factory = OneTimeTaskViewModelFactory(repository)
                    )

                val weeklyViewModel: WeeklyTaskViewModel =
                    viewModel(
                        factory = WeeklyTaskViewModelFactory(repository)
                    )

                AddTaskScreen(
                    oneTimeViewModel = oneTimeViewModel,
                    weeklyViewModel = weeklyViewModel,
                    onBack = {
                        navController.popBackStack("timeline", false)
                    },
                    onSaved ={
                        timelineViewModel.recomputeAvailability()
                        navController.popBackStack("timeline", false)
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onOpenScheduleInput = {
                        navController.navigate("schedule_input")
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("schedule_input") {
                ScheduleInputScreen(
                    onSaved = {
                        //timelineViewModel.recomputeAvailability()   ←将来的にここで走らせるかも
                        navController.popBackStack("timeline", false)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun TimelinePreview() {
    FamilySchedulerTheme {
        TimelineContent(
            state = fakeState
        )
    }
}
*/