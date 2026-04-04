package com.example.familyscheduler.data.mapper

import com.example.familyscheduler.data.local.entity.ChildRoutineEntity
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import java.time.DayOfWeek
import java.time.LocalTime

object ChildRoutineMapper {

    fun toEntity(domain: ChildRoutineInput): ChildRoutineEntity {
        return ChildRoutineEntity(
            name = domain.name,

            wakeUpTime = domain.wakeUpTime.toString(),
            sleepTime = domain.sleepTime.toString(),

            daysOfWeek = domain.daysOfWeek.joinToString(",") { it.name },

            nurseryStart = domain.nurseryStart.toString(),
            nurseryStartEarliest = domain.nurseryStartEarliest.toString(),
            nurseryStartLatest = domain.nurseryStartLatest.toString(),

            nurseryEnd = domain.nurseryEnd.toString(),
            nurseryEndEarliest = domain.nurseryEndEarliest.toString(),
            nurseryEndLatest = domain.nurseryEndLatest.toString()
        )
    }

    fun toDomain(entity: ChildRoutineEntity): ChildRoutineInput {
        return ChildRoutineInput(
            name = entity.name,

            wakeUpTime = LocalTime.parse(entity.wakeUpTime),
            sleepTime = LocalTime.parse(entity.sleepTime),

            daysOfWeek = entity.daysOfWeek
                .split(",")
                .filter { it.isNotBlank() }
                .map { DayOfWeek.valueOf(it) }
                .toSet(),

            nurseryStart = LocalTime.parse(entity.nurseryStart),
            nurseryStartEarliest = LocalTime.parse(entity.nurseryStartEarliest),
            nurseryStartLatest = LocalTime.parse(entity.nurseryStartLatest),

            nurseryEnd = LocalTime.parse(entity.nurseryEnd),
            nurseryEndEarliest = LocalTime.parse(entity.nurseryEndEarliest),
            nurseryEndLatest = LocalTime.parse(entity.nurseryEndLatest)
        )
    }
}