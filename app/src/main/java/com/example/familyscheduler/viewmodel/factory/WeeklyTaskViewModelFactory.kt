package com.example.familyscheduler.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.viewmodel.WeeklyTaskViewModel

class WeeklyTaskViewModelFactory(
    private val repository: HouseholdRequirementRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                WeeklyTaskViewModel::class.java
            )
        ) {
            return WeeklyTaskViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}