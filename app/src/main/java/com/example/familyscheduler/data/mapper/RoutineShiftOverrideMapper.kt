package com.example.familyscheduler.data.mapper

import com.example.familyscheduler.data.local.entity.RoutineShiftOverrideEntity
import com.example.familyscheduler.domain.routine.ChildCareLabel
import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import java.time.LocalDate
import java.time.LocalTime

object RoutineShiftOverrideMapper {

    fun toEntity(domain: RoutineShiftOverride): RoutineShiftOverrideEntity {
        return RoutineShiftOverrideEntity(
            childId = domain.childId,
            date = domain.date.toString(),
            eventType = domain.eventType.name,
            nurseryTime = domain.nurseryTime.toString()
        )
    }

    fun toDomain(entity: RoutineShiftOverrideEntity): RoutineShiftOverride {
        return RoutineShiftOverride(
            childId = entity.childId,
            date = LocalDate.parse(entity.date),
            eventType = ChildCareLabel.valueOf(entity.eventType),
            nurseryTime = LocalTime.parse(entity.nurseryTime)
        )
    }
}