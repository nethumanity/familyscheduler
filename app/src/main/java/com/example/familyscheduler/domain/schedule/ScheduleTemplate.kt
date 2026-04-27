package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import java.time.LocalTime

data class ScheduleTemplate(
    val type: ScheduleType,
    val timeRange: TimeRange
) {
    fun expandToSlots(
        person: Person
    ): List<TimeSlot> {

        val startIndex = TimeAxis.indexOf(timeRange.start)
        val endIndex =
            if (timeRange.end == LocalTime.MIDNIGHT)
                TimeAxis.indices.last + 1
            else
                TimeAxis.indexOf(timeRange.end)

        if (startIndex == -1 || endIndex == -1)
            return emptyList()

        return (startIndex until endIndex).map { index ->
            TimeSlot(
                index = index,
                person = person,
                state = type.state,
                taskIds = emptyList()
            )
        }
    }
}
