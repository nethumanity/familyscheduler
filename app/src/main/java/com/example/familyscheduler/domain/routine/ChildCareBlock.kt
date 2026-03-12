package com.example.familyscheduler.domain.routine

import java.time.DayOfWeek
import java.time.LocalTime

data class ChildCareBlock(
    val daysOfWeek: Set<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val label: ChildCareLabel? = null,
    val flexEarliest: LocalTime? = null,
    val flexLatest: LocalTime? = null,
    val activeChildrenCount: Int
)