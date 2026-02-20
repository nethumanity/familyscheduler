package com.example.familyscheduler.domain.schedule.generator

import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import java.time.LocalDate
import java.time.LocalTime

object DailyStateGenerator {

    fun generate(
        template: DailyTemplate,
        date: LocalDate,
        timeAxis: List<LocalTime>
    ): DailyState {

        // ① UNASSIGNEDで完全配列初期化
        val slots = MutableList(timeAxis.size) { index ->
            TimeSlot(
                index = index,
                person = template.person,
                state = SlotState.UNASSIGNED,
                flexWindow = 0,
                taskName = null
            )
        }

        // ② 各ScheduleTemplateをexpandして上書き
        template.schedules.forEach { schedule ->

            val expanded = schedule.expandToSlots(date, timeAxis)

            expanded.forEach { slot ->
                slots[slot.index] = slot
            }
        }

        return DailyState(
            date = date,
            person = template.person,
            templateName = template.name,
            slots = slots.toList()
        )
    }
}
