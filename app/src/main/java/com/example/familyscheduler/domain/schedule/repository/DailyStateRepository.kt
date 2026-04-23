package com.example.familyscheduler.domain.schedule.repository

import com.example.familyscheduler.domain.schedule.DailyState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyStateRepository {

    fun getByDate(date: LocalDate): Flow<List<DailyState>>

    suspend fun save(state: DailyState)
}