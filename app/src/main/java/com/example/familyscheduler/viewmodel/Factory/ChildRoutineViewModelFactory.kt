package com.example.familyscheduler.viewmodel.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.routine.repository.ChildOverrideRepository
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel

class ChildRoutineViewModelFactory(
    private val repository: ChildRoutineRepository,
    private val overrideRepository: ChildOverrideRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ChildRoutineViewModel(
            repository,
            overrideRepository
        ) as T
    }
}