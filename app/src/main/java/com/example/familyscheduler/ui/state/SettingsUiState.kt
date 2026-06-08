package com.example.familyscheduler.ui.state

data class SettingsUiState(
    val maxChildrenPerAdult: Int = 2,
    val bedtimeSteps: Int = 1,
    val dropOffSteps: Int = 1,
    val pickupSteps: Int =1,
    val showLegend: Boolean = false,
    val showTotal: Boolean = false,
    val timelineStartIndex: Int = 10,
    val timelineEndIndex: Int = 47,
    val timelineStepMinutes: Int = 30,
) {
    init {
        require(timelineStartIndex <= timelineEndIndex)
    }

    val timelineIndices: IntRange
        get() = timelineStartIndex..timelineEndIndex
}