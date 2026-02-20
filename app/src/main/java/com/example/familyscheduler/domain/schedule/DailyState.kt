package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.TimeSlot
import java.time.LocalDate

data class DailyState(
    val date: LocalDate,
    val person: Person,
    val templateName: String,
    val slots: List<TimeSlot>
)
