package com.example.familyscheduler.domain.schedule.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate

interface TemplateRepository {

    suspend fun saveTemplate(template: DailyTemplate)

    suspend fun getTemplates(): List<DailyTemplate>

    suspend fun getTemplatesForPerson(person: Person): List<DailyTemplate>

}