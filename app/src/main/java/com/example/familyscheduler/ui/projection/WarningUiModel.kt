package com.example.familyscheduler.ui.projection

data class WarningUiModel(
    val dialogKey: WarningDialogKey,
    val requirementIds: List<String>,
    val timeText: String,
    val nameText: String,
    val personStates: List<PersonAvailabilityUiModel>,
    val hasProposal: Boolean,
    val cancelApplicable: Boolean,
    val soloApplicable: Boolean
)

data class WarningDialogKey(
    val index: Int,
    val ruleId: String
)