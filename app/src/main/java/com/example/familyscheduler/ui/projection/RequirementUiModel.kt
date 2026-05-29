package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.requirement.RequirementModeToday

data class RequirementUiModel(
    val requirementId: String,
    val startText: String,
    val nameText: String,
    val mode: RequirementModeToday,
    val status: StatusUiModel,
    val isProposalApplied: Boolean = false,
    val canEdit: Boolean,
    val cancelApplicable: Boolean,
    val soloApplicable: Boolean,
    val reverseAssignable: Boolean
)