package com.example.familyscheduler.domain.routine

import java.time.LocalDate

data class RoutineOverrideSnapshot(
    val childOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>,
    val shiftOverrides: List<RoutineShiftOverride>
)