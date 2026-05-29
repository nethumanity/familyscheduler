package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.requirement.RequirementModeToday

data class CareStateUiModel(
    val requirementIds: List<String>,
    val timeText: String,
    val mode: RequirementModeToday,
    val status: StatusUiModel,
    val soloApplicable: Boolean,
    val reverseAssignable: Boolean
)