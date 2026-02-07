package com.example.familyscheduler.domain.template

import com.example.familyscheduler.domain.model.Person
import com.example.familyscheduler.domain.model.SlotState
import java.time.LocalTime

data class ScheduleTemplate(
    val person: Person,
    val start: LocalTime,
    val end: LocalTime,
    val state: SlotState,
    val repeatRule: RepeatRule
)