package com.example.familyscheduler.domain.schedule.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyStateRepository {

    fun getAllFlow(): Flow<Map<Pair<LocalDate, Person>, DailyState>>

    fun getByDate(date: LocalDate): Flow<List<DailyState>>

    suspend fun save(state: DailyState)
}