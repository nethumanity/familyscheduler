package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.person.Person
import java.util.UUID

object DailyTemplateFactory {

    fun create(
        person: Person,
        name: String,
        schedules: List<ScheduleTemplate>
    ): Result<DailyTemplate> {

        validateNoOverlap(schedules)?.let { error ->
            return Result.failure(IllegalArgumentException(error))
        }

        return Result.success(
            DailyTemplate(
                id = UUID.randomUUID(),
                person = person,
                name = name.trim(),
                schedules = schedules.sortedBy { it.timeRange.start },
                repeatRule = RepeatRule.Daily
            )
        )
    }

    private fun validateNoOverlap(
        schedules: List<ScheduleTemplate>
    ): String? {

        val sorted = schedules.sortedBy { it.timeRange.start }

        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]

            if (current.timeRange.overlaps(next.timeRange)) {
                return "Schedule '${current.title}' overlaps with '${next.title}'"
            }
        }

        return null
    }
}
