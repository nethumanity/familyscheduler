package com.example.familyscheduler.ui.utilities

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.familyscheduler.domain.person.Person

data class EditingTarget(
    //val person: Person, //?
    val templateId: String? = null,
    val requirementId: String? = null,
    val childRoutineId: String? = null
) {
    fun isTemplate() = templateId != null
    fun isRequirement() = requirementId != null
    fun isChildRoutine() = childRoutineId != null
}

var editingTarget by mutableStateOf<EditingTarget?>(null)
    private set