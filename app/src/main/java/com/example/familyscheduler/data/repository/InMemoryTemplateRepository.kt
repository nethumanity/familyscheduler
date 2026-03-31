package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository

class InMemoryTemplateRepository : TemplateRepository {

    private val templates = mutableListOf<DailyTemplate>()

    override suspend fun saveTemplate(template: DailyTemplate) {

        val index = templates.indexOfFirst { it.id == template.id }

        if (index >= 0) {
            // 更新
            templates[index] = template
        } else {
            // 新規追加
            templates.add(template)
        }
    }

    override suspend fun getTemplates(): List<DailyTemplate> {
        return templates.toList()
    }

    override suspend fun getTemplatesForPerson(person: Person): List<DailyTemplate> {
        return templates.filter { it.person == person }
    }

    override suspend fun getTemplateFromId(id: String): DailyTemplate? {
        return templates.firstOrNull { it.id == id}
    }

    override suspend fun delete(id: String) {
        templates.removeAll { it.id == id }
    }
}