package com.example.familyscheduler.data.mapper

import com.example.familyscheduler.data.local.dto.TimeSlotDto
import com.example.familyscheduler.data.local.entity.DailyStateEntity
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementSemantics
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

object DailyStateMapper {

    private val gson = Gson()

    fun toEntity(domain: DailyState): DailyStateEntity {

        val dtoList = domain.slots.map {
            TimeSlotDto(
                index = it.index,
                person = it.person.name,
                state = it.state.name,
                taskIds = it.taskIds,
                effectiveSemantics = it.effectiveSemantics.name
            )
        }

        return DailyStateEntity(
            date = domain.date.toString(),
            person = domain.person.name,
            templateName = domain.templateName,
            slotsJson = gson.toJson(dtoList)
        )
    }

    fun toDomain(entity: DailyStateEntity): DailyState {

        val dtoList: List<TimeSlotDto> =
            gson.fromJson(
                entity.slotsJson,
                object : TypeToken<List<TimeSlotDto>>() {}.type
            )

        val slots = dtoList.map {
            TimeSlot(
                index = it.index,
                person = Person.valueOf(it.person),
                state = SlotState.valueOf(it.state),
                taskIds = it.taskIds,
                effectiveSemantics = RequirementSemantics.valueOf(
                    it.effectiveSemantics
                )
            )
        }

        return DailyState(
            date = LocalDate.parse(entity.date),
            person = Person.valueOf(entity.person),
            templateName = entity.templateName,
            slots = slots
        )
    }
}