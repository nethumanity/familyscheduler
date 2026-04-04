package com.example.familyscheduler.data.mapper

import com.example.familyscheduler.data.local.dto.ScheduleTemplateDto
import com.example.familyscheduler.data.local.entity.TemplateEntity
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.ScheduleType
import com.example.familyscheduler.domain.time.TimeRange
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.LocalTime

object TemplateMapper {
    private val gson = Gson()
    fun toEntity(domain: DailyTemplate): TemplateEntity {

        val dtoList = domain.schedules.map {
            ScheduleTemplateDto(
                type = it.type.name,
                start = it.timeRange.start.toString(),
                end = it.timeRange.end.toString()
            )
        }

        val (type, days) = when (val rule = domain.repeatRule) {
            is RepeatRule.None -> "NONE" to null
            is RepeatRule.Daily -> "DAILY" to null
            is RepeatRule.Weekly -> "WEEKLY" to rule.days.joinToString(",") { it.name }
        }

        return TemplateEntity(
            id = domain.id,
            person = domain.person.name,
            name = domain.name,
            schedulesJson = gson.toJson(dtoList),
            repeatType = type,
            repeatDays = days,
            createdAt = domain.createdAt
        )
    }
    fun toDomain(entity: TemplateEntity): DailyTemplate {

        val dtoList: List<ScheduleTemplateDto> =
            gson.fromJson(
                entity.schedulesJson,
                object : TypeToken<List<ScheduleTemplateDto>>() {}.type
            )

        val schedules = dtoList.map {
            ScheduleTemplate(
                type = ScheduleType.valueOf(it.type),
                timeRange = TimeRange(
                    start = LocalTime.parse(it.start),
                    end = LocalTime.parse(it.end)
                )
            )
        }

        val repeatRule = when (entity.repeatType) {
            "NONE" -> RepeatRule.None
            "DAILY" -> RepeatRule.Daily
            "WEEKLY" -> {
                val days = entity.repeatDays
                    ?.takeIf { it.isNotBlank() }
                    ?.split(",")
                    ?.map { DayOfWeek.valueOf(it) }
                    ?.toSet()

                if (days.isNullOrEmpty()) RepeatRule.None
                else RepeatRule.Weekly(days)
            }
            else -> RepeatRule.None
        }

        return DailyTemplate(
            id = entity.id,
            person = Person.valueOf(entity.person),
            name = entity.name,
            schedules = schedules,
            repeatRule = repeatRule,
            createdAt = entity.createdAt
        )
    }
}
