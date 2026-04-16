package com.example.familyscheduler.ui.utilities

data class SettingsUiState(
    val maxChildrenPerAdult: Int = 2,
    val bedtimeMinutes: Int = 30,
    val dropOffMinutes: Int = 30,
    val pickupMinutes: Int =30,
    val showLegend: Boolean = true,
    val showSummary: Boolean = false
)
