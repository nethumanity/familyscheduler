package com.example.familyscheduler.domain.schedule.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate

interface TemplateRepository {

    suspend fun getAll(person: Person): List<DailyTemplate>
}
