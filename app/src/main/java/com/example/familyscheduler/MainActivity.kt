@file: OptIn(ExperimentalMaterial3Api::class)
package com.example.familyscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.familyscheduler.data.repository.InMemoryChildOverrideRepository
import com.example.familyscheduler.data.repository.InMemoryChildRoutineRepository
import com.example.familyscheduler.data.repository.InMemoryDailyStateRepository
import com.example.familyscheduler.data.repository.InMemoryHouseholdRequirementRepository
import com.example.familyscheduler.data.repository.InMemoryTemplateRepository
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.routine.CareCapacityCalculator
import com.example.familyscheduler.domain.routine.ChildCareRuleConverter
import com.example.familyscheduler.domain.routine.ChildRoutineBuilder
import com.example.familyscheduler.domain.routine.RoutineResolver
import com.example.familyscheduler.ui.components.ChildScreen
import com.example.familyscheduler.ui.components.SettingsScreen
import com.example.familyscheduler.ui.inputs.AddTaskScreen
import com.example.familyscheduler.ui.inputs.ScheduleInputScreen
import com.example.familyscheduler.ui.manager.MainSheet
import com.example.familyscheduler.ui.theme.FamilySchedulerTheme
import com.example.familyscheduler.ui.timeline.FooterBar
import com.example.familyscheduler.ui.timeline.HeaderBar
import com.example.familyscheduler.ui.timeline.TimelineScreen
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel
import com.example.familyscheduler.viewmodel.Factory.ChildRoutineViewModelFactory
import com.example.familyscheduler.viewmodel.Factory.OneTimeTaskViewModelFactory
import com.example.familyscheduler.viewmodel.Factory.TemplateEditViewModelFactory
import com.example.familyscheduler.viewmodel.Factory.TimelineViewModelFactory
import com.example.familyscheduler.viewmodel.Factory.WeeklyTaskViewModelFactory
import com.example.familyscheduler.viewmodel.OneTimeTaskViewModel
import com.example.familyscheduler.viewmodel.TemplateEditViewModel
import com.example.familyscheduler.viewmodel.TimelineViewModel
import com.example.familyscheduler.viewmodel.WeeklyTaskViewModel
import java.time.LocalDate

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

    val templateRepository = remember { InMemoryTemplateRepository() }
    val dailyStateRepository = remember { InMemoryDailyStateRepository() }
    val householdRequirementRepository = remember { InMemoryHouseholdRequirementRepository() }
    val childRepository = remember { InMemoryChildRoutineRepository() }
    val overrideRepository = remember { InMemoryChildOverrideRepository() }

    val factory = TimelineViewModelFactory(
        templateRepository = templateRepository,
        dailyStateRepository = dailyStateRepository,
        householdRequirementRepository = householdRequirementRepository,
        childRoutineRepository = childRepository,
        routineResolver = RoutineResolver(
            overrideRepository = overrideRepository
        ),
        childRoutineBuilder = ChildRoutineBuilder(),
        childCareRuleConverter = ChildCareRuleConverter(
            capacityCalculator = CareCapacityCalculator(),
            allowedPersons = Person.values().toSet()
        )
    )

    val timelineViewModel: TimelineViewModel =
        viewModel(factory = factory)
    val childRoutineViewModel: ChildRoutineViewModel =
        viewModel(factory = ChildRoutineViewModelFactory(childRepository, overrideRepository))

    var sheet by remember { mutableStateOf<MainSheet?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Scaffold(
        topBar = {

            when (currentRoute) {

                "timeline" -> {

                    HeaderBar(
                        date = timelineViewModel.currentDate.collectAsState().value,
                        onPreviousDay = {
                            timelineViewModel.moveToPreviousDay()   // changeDate(currentDate.minusDays(1))
                        },
                        onNextDay = {
                            timelineViewModel.moveToNextDay()   // changeDate(currentDate.plusDays(1))
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
                // 今後ここに追加していく（route = calender）

                onChildClick = {
                    sheet = MainSheet.CHILD
                },
                onTodayClick = {
                    timelineViewModel.changeDate(LocalDate.now())
                    navController.popBackStack("timeline", false)
                },
                onAddClick = {
                    navController.navigate("add_task")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
    ) { padding ->

        Box(Modifier.padding(padding)) {

            NavHost(
                navController = navController,
                startDestination = "timeline",
            ) {

                composable("timeline") {

                    TimelineScreen(
                        viewModel = timelineViewModel
                    )
                }

                composable("calender") {

                }

                composable("add_task") {

                    val oneTimeViewModel: OneTimeTaskViewModel =
                        viewModel(
                            factory = OneTimeTaskViewModelFactory(householdRequirementRepository)
                        )

                    val weeklyViewModel: WeeklyTaskViewModel =
                        viewModel(
                            factory = WeeklyTaskViewModelFactory(householdRequirementRepository)
                        )

                    AddTaskScreen(
                        oneTimeViewModel = oneTimeViewModel,
                        weeklyViewModel = weeklyViewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onSaved = {
                            timelineViewModel.refreshAvailability()
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

                    val templateEditViewModel: TemplateEditViewModel =
                        viewModel(
                            factory = TemplateEditViewModelFactory(templateRepository)
                        )

                    ScheduleInputScreen(
                        viewModel = templateEditViewModel,
                        onSaved = {
                            timelineViewModel.reloadCurrentDate()
                            navController.popBackStack("timeline", false)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            sheet?.let {

                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = { sheet = null }
                ) {

                    when (it) {

                        MainSheet.CHILD -> {

                            ChildScreen(
                                viewModel = childRoutineViewModel,
                                currentDate = timelineViewModel.currentDate.collectAsState().value,
                                onClose = {
                                    timelineViewModel.onChildRoutineChanged()
                                    sheet = null
                                },
                                onToggle = { timelineViewModel.onChildRoutineChanged() }
                            )
                        }
                    }
                }
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