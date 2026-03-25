package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import java.util.UUID

data class DailyTemplate(   //1つのDailyTemplateは複数のScheduleTemplateからなる
    val id: String = UUID.randomUUID().toString(),
    val person: Person,
    val name: String,
    val schedules: List<ScheduleTemplate>,
    val repeatRule: RepeatRule,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun expandToSlots(): List<TimeSlot> {

        val baseSlots =
            TimeAxis.indices.map { index ->
                TimeSlot(
                    index = index,
                    person = person,
                    state = SlotState.UNASSIGNED,
                    flexWindow = FlexWindowParameters(0, 0),
                    taskName = emptyList()
                )
            }.toMutableList()

        schedules.sortedBy { it.type.priority }.forEach { schedule ->

            val expanded = schedule.expandToSlots(person)

            expanded.forEach {
                baseSlots[it.index] = it
            }
        }

        return baseSlots
    }
}
