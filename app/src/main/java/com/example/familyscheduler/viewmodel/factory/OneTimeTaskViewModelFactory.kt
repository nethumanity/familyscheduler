package com.example.familyscheduler.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.viewmodel.OneTimeTaskViewModel

class OneTimeTaskViewModelFactory(
    private val repository: HouseholdRequirementRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                OneTimeTaskViewModel::class.java
            )
        ) {
            return OneTimeTaskViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}