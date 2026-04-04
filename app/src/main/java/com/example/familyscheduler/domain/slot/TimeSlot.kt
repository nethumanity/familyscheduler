package com.example.familyscheduler.domain.slot

import com.example.familyscheduler.domain.person.Person

data class TimeSlot(
    val index: Int,
    val person: Person,
    val state: SlotState,
    val flexWindow: FlexWindowParameters,   //複数の予定に対しひとつのflexWindow問題
    val taskName: List<String>
)