package com.example.familyscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.familyscheduler.ui.components.ScheduleInputScreen
import com.example.familyscheduler.ui.components.SettingsScreen
import com.example.familyscheduler.ui.theme.FamilySchedulerTheme
import com.example.familyscheduler.ui.timeline.FooterBar
import com.example.familyscheduler.ui.timeline.TimelineScreen
import com.example.familyscheduler.viewmodel.MainViewModel
import com.example.familyscheduler.viewmodel.TimelineViewModel

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

    val timelineViewModel:
            TimelineViewModel = viewModel()
    val mainViewModel:
            MainViewModel = viewModel()

    Scaffold(
        bottomBar = {
            FooterBar(
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
                    viewModel = timelineViewModel,
                    viewModel_toBeRemoved = mainViewModel
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
                        timelineViewModel.loadForDate(
                            timelineViewModel.currentDate.value
                        )
                        navController.popBackStack("timeline", false)
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