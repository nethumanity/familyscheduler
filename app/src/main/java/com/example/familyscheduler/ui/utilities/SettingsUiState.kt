package com.example.familyscheduler.ui.utilities

data class SettingsUiState(
    val timelineStartIndex: Int = 10,
    val timelineEndIndex: Int = 47,
    val timelineStepMinutes: Int = 30,
    val maxChildrenPerAdult: Int = 2,
    val bedtimeMinutes: Int = 30,
    val dropOffMinutes: Int = 30,
    val pickupMinutes: Int =30,
    val showLegend: Boolean = false,
    val showTotal: Boolean = false
)
