package com.example.familyscheduler.domain.schedule.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyState
import java.time.LocalDate

interface DailyStateRepository {

    suspend fun save(state: DailyState)

    suspend fun get(date: LocalDate): List<DailyState>

    suspend fun get(date: LocalDate, person: Person): DailyState?

}