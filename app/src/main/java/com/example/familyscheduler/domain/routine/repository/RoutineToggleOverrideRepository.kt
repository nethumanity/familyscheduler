package com.example.familyscheduler.domain.routine.repository

import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RoutineToggleOverrideRepository {

    fun getAllFlow(): Flow<Map<Pair<String, LocalDate>, ChildTodayRoutine>>

    fun getByDate(date: LocalDate): Flow<Map<Pair<String, LocalDate>, ChildTodayRoutine>>

    suspend fun replace(
        childId: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    )

    suspend fun deleteAllByChildId(childId: String)
}