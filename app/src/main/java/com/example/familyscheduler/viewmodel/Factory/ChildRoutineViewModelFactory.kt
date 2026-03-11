package com.example.familyscheduler.viewmodel.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.requirement.repository.ChildOverrideRepository
import com.example.familyscheduler.domain.requirement.repository.ChildRoutineRepository
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