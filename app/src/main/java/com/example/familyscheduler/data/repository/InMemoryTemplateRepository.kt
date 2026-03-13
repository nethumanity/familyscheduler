package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository

class InMemoryTemplateRepository : TemplateRepository {

    private val templates = mutableListOf<DailyTemplate>()

    override suspend fun saveTemplate(template: DailyTemplate) {
        templates.removeAll {
            it.person == template.person && it.name == template.name
        }
        templates.add(template)
    }

    override suspend fun getTemplates(): List<DailyTemplate> {
        return templates.toList()
    }

    override suspend fun getTemplatesForPerson(person: Person): List<DailyTemplate> {
        return templates.filter { it.person == person }
    }
}