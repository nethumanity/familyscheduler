package com.example.familyscheduler.domain.routine

import java.time.LocalTime

data class ChildCareEvent(
    val eventId: String,
    val time: LocalTime,
    val duration: Int,
    val label: ChildCareLabel,
    val flexEarliest: LocalTime,
    val flexLatest: LocalTime,
    val childIds: List<String>
)
