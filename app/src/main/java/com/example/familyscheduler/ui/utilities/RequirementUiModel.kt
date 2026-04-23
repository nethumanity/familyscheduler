package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.slot.SlotState

data class RequirementUiModel(
    val id: String,
    val name: String,
    val startIndex: Int,
    val targetState: SlotState,
    val mode: RequirementModeToday,
    val canEdit: Boolean,
    val isProposalApplied: Boolean = false
)

