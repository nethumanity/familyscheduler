package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class ScheduleTemplate(            //1つのScheduleTemplateは1つのDailyTemplateに紐づくのがわかりやすい
    val id: UUID = UUID.randomUUID(),   //DailyTemplateとの紐づけのため？
    val person: Person,                 //いらない？
    val title: String,                  //いらない？
    val type: ScheduleType,
    val timeRange: TimeRange,
    val repeatRule: RepeatRule          //いらない？
) {
    fun expandToSlots(
        date: LocalDate,
        timeAxis: List<LocalTime>
    ): List<TimeSlot> {

        if (!repeatRule.appliesTo(date)) return emptyList()

        val startIndex = TimeAxis.indexOf(timeRange.start)
        val endIndex = TimeAxis.indexOf(timeRange.end)
        val slotState = type.toSlotState()

        return (startIndex until endIndex).map { index ->
            TimeSlot(
                index = index,
                person = person,
                state = slotState,
                flexWindow = type.flexWindow,
                taskName = null
            )
        }
    }
}
