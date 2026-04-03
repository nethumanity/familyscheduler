package com.example.familyscheduler.domain.schedule.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {

    fun getAllFlow(): Flow<List<DailyTemplate>>

    fun getTemplateById(id: String): Flow<DailyTemplate?>

    fun getTemplatesForPerson(person: Person): Flow<List<DailyTemplate>>

    suspend fun save(template: DailyTemplate)

    suspend fun delete(id: String)
}