package com.example.familyscheduler.viewmodel.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.viewmodel.OneTimeAppointmentViewModel

class OneTimeAppointmentViewModelFactory(
    private val repository: HouseholdRequirementRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(
                OneTimeAppointmentViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return OneTimeAppointmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}