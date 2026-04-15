package com.example.familyscheduler.domain.routine

import java.time.LocalDate
import java.time.LocalTime

data class RoutineShiftOverride(
    val childId: String,
    val date: LocalDate,
    val eventType: ChildCareLabel,
    val nurseryTime: LocalTime
)