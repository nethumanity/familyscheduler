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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.familyscheduler.data.local.AppDatabase
import com.example.familyscheduler.data.repository.RoomChildOverrideRepository
import com.example.familyscheduler.data.repository.RoomChildRoutineRepository
import com.example.familyscheduler.data.repository.RoomDailyStateRepository
import com.example.familyscheduler.data.repository.RoomHouseholdRequirementRepository
import com.example.familyscheduler.data.repository.RoomRequirementOverrideRepository
import com.example.familyscheduler.data.repository.RoomTemplateRepository
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementBuilder
import com.example.familyscheduler.domain.routine.CareCapacityCalculator
import com.example.familyscheduler.domain.routine.ChildCareRuleConverter
import com.example.familyscheduler.domain.routine.ChildRoutineBuilder
import com.example.familyscheduler.domain.routine.RoutineResolver
import com.example.familyscheduler.ui.components.ChildListSheet
import com.example.familyscheduler.ui.components.DailyOverviewSheet
import com.example.familyscheduler.ui.components.SettingsScreen
import com.example.familyscheduler.ui.inputs.AddTaskScreen
import com.example.familyscheduler.ui.inputs.ChildRoutineInputScreen
import com.example.familyscheduler.ui.inputs.ScheduleInputScreen
import com.example.familyscheduler.ui.manager.MainSheet
import com.example.familyscheduler.ui.theme.FamilySchedulerTheme
import com.example.familyscheduler.ui.timeline.FooterBar
import com.example.familyscheduler.ui.timeline.HeaderBar
import com.example.familyscheduler.ui.timeline.TimelineScreen
import com.example.familyscheduler.ui.utilities.UiEvent
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
import kotlinx.coroutines.flow.merge
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

    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app-db"
        ).build()
    }

    val templateRepository = remember { RoomTemplateRepository(db.templateDao()) }
    val dailyStateRepository = remember { RoomDailyStateRepository(db.dailyStateDao()) }
    val householdRequirementRepository = remember { RoomHouseholdRequirementRepository(db.householdRequirementDao()) }
    val requirementOverrideRepository = remember { RoomRequirementOverrideRepository(db.requirementOverrideDao()) }
    val childRepository = remember { RoomChildRoutineRepository(db.childRoutineDao()) }
    val childOverrideRepository = remember { RoomChildOverrideRepository(db.childOverrideDao()) }

    val factory = TimelineViewModelFactory(
        templateRepository = templateRepository,
        dailyStateRepository = dailyStateRepository,
        householdRequirementRepository = householdRequirementRepository,
        requirementOverrideRepository = requirementOverrideRepository,
        childRoutineRepository = childRepository,
        childOverrideRepository = childOverrideRepository,
        routineResolver = RoutineResolver(),
        childRoutineBuilder = ChildRoutineBuilder(),
        childCareRuleConverter = ChildCareRuleConverter(
            capacityCalculator = CareCapacityCalculator(),
            allowedPersons = Person.values().toSet()
        ),
        requirementBuilder = RequirementBuilder()
    )

    val timelineViewModel: TimelineViewModel =
        viewModel(factory = factory)
    val childRoutineViewModel: ChildRoutineViewModel =
        viewModel(factory = ChildRoutineViewModelFactory(childRepository, childOverrideRepository))

    var sheet by remember { mutableStateOf<MainSheet?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val snackbarHostState = remember { SnackbarHostState() }

    val currentDate by timelineViewModel.currentDate.collectAsState()

    LaunchedEffect(Unit) {
        merge(
            timelineViewModel.events,
            childRoutineViewModel.events
        ).collect { event ->

            when (event) {

                is UiEvent.ShowUndoDelete -> {

                    snackbarHostState.currentSnackbarData?.dismiss()

                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "元に戻す",
                        duration = SnackbarDuration.Short
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        event.onUndo()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {

            when (currentRoute) {

                "timeline" -> {

                    HeaderBar(
                        date = currentDate,
                        onPreviousDay = {
                            timelineViewModel.changeDate(currentDate.minusDays(1))
                        },
                        onNextDay = {
                            timelineViewModel.changeDate(currentDate.plusDays(1))
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
                viewModel = timelineViewModel,

                onOverviewClick = {
                    sheet = MainSheet.DAILY_OVERVIEW
                },

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
                        viewModel = timelineViewModel,
                        onAddClick = { person ->
                            navController.navigate("schedule_input/${person.name}")
                        },
                        onEditTemplate = { templateId, person ->
                            timelineViewModel.startEditTemplate(templateId)
                            navController.navigate("schedule_input/${person.name}")
                        }
                    )
                }

                composable("child_input") {

                    val viewModel = childRoutineViewModel

                    val editingTarget by viewModel.editingTarget.collectAsState()

                    LaunchedEffect(editingTarget?.childRoutineId) { //元は、引数：Unit
                        val id = editingTarget?.childRoutineId

                        if (id != null) {
                            viewModel.load(id)
                            viewModel.clearEditingTarget()
                        }
                    }

                    ChildRoutineInputScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onSaved = {
                            navController.popBackStack("timeline", false)
                        }
                    )
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

                    val editingTarget by timelineViewModel.editingTarget.collectAsState()
                    val uiState by timelineViewModel.uiState.collectAsState()

                    var selectedTab by remember { mutableStateOf(0) }

                    LaunchedEffect(editingTarget?.requirementId) {
                        val id = editingTarget?.requirementId ?: return@LaunchedEffect

                        val rule = uiState.rules.firstOrNull { it.id == id }
                            ?: return@LaunchedEffect

                        rule.let {
                            selectedTab = if (it.date != null) 0 else 1

                            when {
                                it.date != null -> oneTimeViewModel.load(it)
                                else -> weeklyViewModel.load(it)
                            }
                        }

                        timelineViewModel.clearEditingTarget()
                    }

                    AddTaskScreen(
                        oneTimeViewModel = oneTimeViewModel,
                        weeklyViewModel = weeklyViewModel,
                        selectedTab = selectedTab,
                        onTabChange = { selectedTab = it },
                        onBack = {
                            navController.popBackStack()
                        },
                        onSaved = {
                            navController.popBackStack("timeline", false)
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onOpenScheduleInput = {
                            navController.navigate("schedule_input/${Person.FATHER.name}")
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("schedule_input/{personName}") { backStackEntry ->

                    val personName = backStackEntry.arguments?.getString("personName")
                    val person = Person.valueOf(personName!!)

                    val templateEditViewModel: TemplateEditViewModel =
                        viewModel(
                            factory = TemplateEditViewModelFactory(
                                templateRepository,
                                person
                            )
                        )

                    val editingTarget by timelineViewModel.editingTarget.collectAsState()
                    val uiState by timelineViewModel.uiState.collectAsState()

                    LaunchedEffect(editingTarget?.templateId) {
                        val target = editingTarget ?: return@LaunchedEffect

                        if (target.isTemplate()) {
                            val template = uiState.templates.firstOrNull { it.id == target.templateId }
                                ?: return@LaunchedEffect
                            template.let {
                                templateEditViewModel.load(it)
                            }
                            timelineViewModel.clearEditingTarget()
                        }
                    }

                    ScheduleInputScreen(
                        viewModel = templateEditViewModel,
                        onSaved = {
                            timelineViewModel.dismissTemplateSheet()
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
                            ChildListSheet(
                                viewModel = childRoutineViewModel,
                                currentDate = currentDate,
                                onAddClick = {
                                    sheet = null
                                    navController.navigate("child_input")
                                },
                                onEditChildRoutine = { childName ->
                                    childRoutineViewModel.startEditChildRoutine(childName)
                                    sheet = null
                                    navController.navigate("child_input")
                                }
                            )
                        }

                        MainSheet.DAILY_OVERVIEW -> {
                            DailyOverviewSheet(
                                viewModel = timelineViewModel,
                                onEditRequirement = { ruleId ->
                                    timelineViewModel.startEditRequirement(ruleId)
                                    sheet = null
                                    navController.navigate("add_task")
                                }
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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