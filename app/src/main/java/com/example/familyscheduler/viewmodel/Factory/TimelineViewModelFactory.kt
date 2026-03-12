package com.example.familyscheduler.viewmodel.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.routine.ChildCareRuleConverter
import com.example.familyscheduler.domain.routine.ChildRoutineBuilder
import com.example.familyscheduler.domain.routine.RoutineResolver
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.viewmodel.TimelineViewModel

class TimelineViewModelFactory(
    private val repository: HouseholdRequirementRepository,
    private val childRoutineRepository: ChildRoutineRepository,
    private val routineResolver: RoutineResolver,
    private val childRoutineBuilder: ChildRoutineBuilder,
    private val childCareRuleConverter: ChildCareRuleConverter
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {

            return TimelineViewModel(
                repository = repository,
                childRoutineRepository = childRoutineRepository,
                routineResolver = routineResolver,
                childRoutineBuilder = childRoutineBuilder,
                childCareRuleConverter = childCareRuleConverter
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}