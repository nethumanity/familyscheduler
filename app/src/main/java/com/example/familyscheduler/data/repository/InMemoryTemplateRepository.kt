package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate

object InMemoryTemplateRepository {

    private val templates = mutableListOf<DailyTemplate>()

    fun saveTemplate(template: DailyTemplate) {
        templates.removeAll {
            it.person == template.person && it.name == template.name
        }
        templates.add(template)
    }

    fun getTemplates(): List<DailyTemplate> {
        return templates.toList()
    }

    fun getTemplatesForPerson(person: Person): List<DailyTemplate> {
        return templates.filter { it.person == person }
    }
}
