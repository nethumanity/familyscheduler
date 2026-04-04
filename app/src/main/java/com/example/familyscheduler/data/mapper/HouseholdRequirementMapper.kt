package com.example.familyscheduler.data.mapper

import com.example.familyscheduler.data.local.entity.HouseholdRequirementEntity
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

object HouseholdRequirementMapper {

    fun toEntity(domain: HouseholdRequirementRule): HouseholdRequirementEntity {
        return HouseholdRequirementEntity(
            id = domain.id,
            source = domain.source.name,
            taskName = domain.taskName,
            targetState = domain.targetState.name,
            requiredCount = domain.requiredCount,

            allowedPersons = domain.allowedPersons.joinToString(",") { it.name },

            flexBackward = domain.flexWindowSlots.backward,
            flexForward = domain.flexWindowSlots.forward,

            date = domain.date?.toString(),
            daysOfWeek = domain.daysOfWeek?.joinToString(",") { it.name },

            startTime = domain.timeRange.start.toString(),
            endTime = domain.timeRange.end.toString(),

            createdAt = domain.createdAt
        )
    }

    fun toDomain(entity: HouseholdRequirementEntity): HouseholdRequirementRule {
        return HouseholdRequirementRule(
            id = entity.id,
            source = RequirementSource.valueOf(entity.source),
            taskName = entity.taskName,
            targetState = SlotState.valueOf(entity.targetState),
            requiredCount = entity.requiredCount,

            allowedPersons = entity.allowedPersons
                .split(",")
                .filter { it.isNotBlank() }
                .map { Person.valueOf(it) }
                .toSet(),

            flexWindowSlots = FlexWindowParameters(
                backward = entity.flexBackward,
                forward = entity.flexForward
            ),

            date = entity.date?.let { LocalDate.parse(it) },

            daysOfWeek = entity.daysOfWeek
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.map { DayOfWeek.valueOf(it) }
                ?.toSet(),

            timeRange = TimeRange(
                start = LocalTime.parse(entity.startTime),
                end = LocalTime.parse(entity.endTime)
            ),

            createdAt = entity.createdAt
        )
    }
}