package com.example.familyscheduler.viewmodel.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.requirement.repository.ChildRoutineRepository
import com.example.familyscheduler.viewmodel.ChildRoutineViewModel

class ChildRoutineViewModelFactory(
    private val repository: ChildRoutineRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ChildRoutineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChildRoutineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}