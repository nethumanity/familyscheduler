package com.example.familyscheduler.ui.utilities

data class SettingsUiState(
    val timelineStartIndex: Int = 10,
    val timelineEndIndex: Int = 47,
    val timelineStepMinutes: Int = 30,
    val maxChildrenPerAdult: Int = 2,
    val bedtimeSteps: Int = 1,
    val dropOffSteps: Int = 1,
    val pickupSteps: Int =1,
    val showLegend: Boolean = false,
    val showTotal: Boolean = false
)
