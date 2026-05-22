package com.example.familyscheduler.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import com.example.familyscheduler.viewmodel.TemplateEditViewModel

class TemplateEditViewModelFactory(
    private val repository: TemplateRepository,
    private val person: Person
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TemplateEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TemplateEditViewModel(repository, person) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}