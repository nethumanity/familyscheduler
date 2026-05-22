package com.example.familyscheduler.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.routine.repository.RoutineToggleOverrideRepository
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.domain.routine.repository.RoutineShiftOverrideRepository
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel

class ChildRoutineViewModelFactory(
    private val repository: ChildRoutineRepository,
    private val routineToggleOverrideRepository: RoutineToggleOverrideRepository,
    private val routineShiftOverrideRepository: RoutineShiftOverrideRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ChildRoutineViewModel(
            repository,
            routineToggleOverrideRepository,
            routineShiftOverrideRepository
        ) as T
    }
}