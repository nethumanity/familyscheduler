package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import kotlinx.coroutines.launch

class TemplateEditViewModel(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    fun saveTemplate(template: DailyTemplate) {

        viewModelScope.launch {

            templateRepository
                .saveTemplate(template)

            Log.d(
                "TemplateEdit",
                "Template saved"
            )
        }
    }

    fun debugPrintTemplates() {
        viewModelScope.launch {
            val templates =
                templateRepository.getTemplates()
            templates.forEach {
                Log.d("TemplateDebug", it.toString())
            }
        }
    }
}
