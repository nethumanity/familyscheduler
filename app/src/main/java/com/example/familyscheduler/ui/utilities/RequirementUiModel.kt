package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.slot.SlotState

data class RequirementUiModel(
    val id: String,
    val name: String,
    val startIndex: Int,
    val targetState: SlotState,
    val mode: RequirementModeToday
)

/* 将来向け
data class RequirementUiModel(
    val time: String,
    val name: String,
    val mode: RequirementModeToday,
    val assigned: List<String>,
    val warningCount: Int?,
    val requiredCount: Int
) */
