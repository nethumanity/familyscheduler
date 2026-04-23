package com.example.familyscheduler.data.mapper


import com.example.familyscheduler.data.local.entity.RoutineToggleOverrideEntity
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import java.time.LocalDate

object RoutineToggleOverrideMapper {

    fun toEntity(
        childId: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    ): RoutineToggleOverrideEntity {
        return RoutineToggleOverrideEntity(
            childId = childId,
            date = date.toString(),
            routine = routine.name
        )
    }

    fun toDomain(entity: RoutineToggleOverrideEntity): Pair<Pair<String, LocalDate>, ChildTodayRoutine> {
        return (entity.childId to LocalDate.parse(entity.date)) to
                ChildTodayRoutine.valueOf(entity.routine)
    }
}