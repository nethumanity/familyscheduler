package com.example.familyscheduler.data.mapper


import com.example.familyscheduler.data.local.entity.ChildOverrideEntity
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import java.time.LocalDate

object ChildOverrideMapper {

    fun toEntity(
        childId: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    ): ChildOverrideEntity {
        return ChildOverrideEntity(
            childId = childId,
            date = date.toString(),
            routine = routine.name
        )
    }

    fun toDomain(entity: ChildOverrideEntity): Pair<Pair<String, LocalDate>, ChildTodayRoutine> {
        return (entity.childId to LocalDate.parse(entity.date)) to
                ChildTodayRoutine.valueOf(entity.routine)
    }
}