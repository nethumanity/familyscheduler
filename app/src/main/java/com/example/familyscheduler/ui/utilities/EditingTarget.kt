package com.example.familyscheduler.ui.utilities

data class EditingTarget(
    val templateId: String? = null,
    val requirementId: String? = null,
    val childRoutineId: String? = null
) {
    fun isTemplate() = templateId != null
    fun isRequirement() = requirementId != null
    fun isChildRoutine() = childRoutineId != null
}
