package com.example.familyscheduler.domain.slot

import com.example.familyscheduler.domain.person.Person

data class TimeSlot(
    val index: Int,
    val person: Person,
    val state: SlotState,
    val flexWindow: FlexWindowParameters,
    val taskName: List<String?>
)