package com.example.familyscheduler.domain.schedule

import java.util.UUID

object ScheduleFactory {

    fun fromInput(
        input: ScheduleInput,
        resolver: (UUID) -> ScheduleType
    ): ScheduleTemplate {

        val type = resolver(input.scheduleTypeId)

        return ScheduleTemplate(
            person = input.person,
            title = input.title.trim(),
            type = type,
            timeRange = TimeRange(input.startTime, input.endTime),
            repeatRule = input.repeatRule
        )
    }
}
