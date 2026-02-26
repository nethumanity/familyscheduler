package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalDate
import java.util.UUID

data class DailyTemplate(   //1つのDailyTemplateは複数のScheduleTemplateからなる
    val id: UUID = UUID.randomUUID(),
    val person: Person,
    val name: String,
    val schedules: List<ScheduleTemplate>,
    val repeatRule: RepeatRule
) {
    fun expandToSlots(date: LocalDate): List<TimeSlot> {

        if (!repeatRule.appliesTo(date))
            return emptyList()

        val baseSlots =
            TimeAxis.indices.map { index ->
                TimeSlot(
                    index = index,
                    person = person,
                    state = SlotState.UNASSIGNED,
                    flexWindow = 0,
                    taskName = null
                )
            }.toMutableList()

        schedules.forEach { schedule ->
            val expanded = schedule.expandToSlots(person)

            expanded.forEach {
                baseSlots[it.index] = it
            }
        }

        return baseSlots
    }
}
