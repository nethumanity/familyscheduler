package com.example.familyscheduler.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import com.example.familyscheduler.viewmodel.TemplateEditViewModel

class TemplateEditViewModelFactory(
    private val repository: TemplateRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TemplateEditViewModel::class.java)) {
            return TemplateEditViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}