package com.example.familyscheduler.viewmodel.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.requirement.RequirementBuilder
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.requirement.repository.RequirementOverrideRepository
import com.example.familyscheduler.domain.routine.ChildCareRuleConverter
import com.example.familyscheduler.domain.routine.ChildRoutineBuilder
import com.example.familyscheduler.domain.routine.RoutineResolver
import com.example.familyscheduler.domain.routine.repository.ChildOverrideRepository
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.domain.routine.repository.RoutineShiftOverrideRepository
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import com.example.familyscheduler.ui.utilities.repository.SettingsRepository
import com.example.familyscheduler.viewmodel.TimelineViewModel

class TimelineViewModelFactory(
    private val templateRepository: TemplateRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val householdRequirementRepository: HouseholdRequirementRepository,
    private val requirementOverrideRepository: RequirementOverrideRepository,
    private val childRoutineRepository: ChildRoutineRepository,
    private val childOverrideRepository: ChildOverrideRepository,
    private val routineShiftOverrideRepository: RoutineShiftOverrideRepository,
    private val routineResolver: RoutineResolver,
    private val childRoutineBuilder: ChildRoutineBuilder,
    private val childCareRuleConverter: ChildCareRuleConverter,
    private val requirementBuilder: RequirementBuilder,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {

            return TimelineViewModel(
                templateRepository = templateRepository,
                dailyStateRepository = dailyStateRepository,
                householdRequirementRepository = householdRequirementRepository,
                requirementOverrideRepository = requirementOverrideRepository,
                childRoutineRepository = childRoutineRepository,
                childOverrideRepository = childOverrideRepository,
                routineShiftOverrideRepository = routineShiftOverrideRepository,
                routineResolver = routineResolver,
                childRoutineBuilder = childRoutineBuilder,
                childCareRuleConverter = childCareRuleConverter,
                requirementBuilder = requirementBuilder,
                settingsRepository = settingsRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}