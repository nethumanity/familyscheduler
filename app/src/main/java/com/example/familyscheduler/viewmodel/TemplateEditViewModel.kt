package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.data.repository.InMemoryTemplateRepository
import com.example.familyscheduler.domain.schedule.DailyTemplate
import kotlinx.coroutines.launch

class TemplateEditViewModel : ViewModel() {

    fun saveTemplate(template: DailyTemplate) {

        viewModelScope.launch {

            InMemoryTemplateRepository
                .saveTemplate(template)

            Log.d(
                "TemplateEdit",
                "Template saved"
            )
        }
    }
}
