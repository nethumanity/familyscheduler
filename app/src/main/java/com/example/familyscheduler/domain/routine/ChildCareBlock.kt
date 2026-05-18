package com.example.familyscheduler.domain.routine

import java.time.LocalDate
import java.time.LocalTime

data class ChildCareBlock(
    val id: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val label: ChildCareLabel? = null,
    val flexEarliest: LocalTime? = null,
    val flexLatest: LocalTime? = null,
    val activeChildrenCount: Int
)