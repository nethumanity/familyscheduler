package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.requirement.RequirementModeToday

data class RequirementDisplayState(
    val time: String,
    val name: String,
    val mode: RequirementModeToday,
    val assigned: List<String>,
    val warningCount: Int?,
    val requiredCount: Int
)