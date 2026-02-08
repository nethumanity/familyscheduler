package com.example.familyscheduler.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class TimeSlot(
    val date: LocalDate,
    val index: Int,
    val person: Person,
    val state: SlotState,
    val flexWindow: Int = 0,
    val taskName: String?
)
