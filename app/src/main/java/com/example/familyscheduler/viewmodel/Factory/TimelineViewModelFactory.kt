package com.example.familyscheduler.viewmodel.Factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.viewmodel.TimelineViewModel

class TimelineViewModelFactory(
    private val repository: HouseholdRequirementRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(TimelineViewModel::class.java)) {
            return TimelineViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}