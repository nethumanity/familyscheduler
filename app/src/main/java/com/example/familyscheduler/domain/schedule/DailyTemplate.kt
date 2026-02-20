package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.person.Person
import java.util.UUID

data class DailyTemplate(   //1つのDailyTemplateは複数のScheduleTemplateからなる
    val id: UUID = UUID.randomUUID(),
    val person: Person,
    val name: String,
    val schedules: List<ScheduleTemplate>,
    val repeatRule: RepeatRule  //null可、RepeatRule?にする？
)
