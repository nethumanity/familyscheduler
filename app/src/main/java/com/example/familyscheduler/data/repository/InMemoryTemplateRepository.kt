package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryTemplateRepository : TemplateRepository {

    private val _templates = MutableStateFlow<List<DailyTemplate>>(emptyList())

    override fun getAllFlow(): Flow<List<DailyTemplate>> {
        return _templates
    }

    // 編集画面用（いらない？）
    override fun getTemplateById(id: String): Flow<DailyTemplate?> {
        return _templates
            .map { list ->
                list.firstOrNull { it.id == id }
            }
    }

    // いらない？
    override fun getTemplatesForPerson(person: Person): Flow<List<DailyTemplate>> {
        return _templates
            .map { list ->
                list.filter { it.person == person }
            }
    }

    override suspend fun save(template: DailyTemplate) {
        _templates.update { old ->
            val filtered = old.filterNot { it.id == template.id }

            filtered + template
        }
    }

    override suspend fun delete(id: String) {
        _templates.update { old ->
            val filtered = old.filterNot { it.id == id }

            filtered
        }
    }
}