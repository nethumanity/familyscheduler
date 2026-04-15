package com.example.familyscheduler.domain.routine.repository

import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ChildOverrideRepository {

    fun getAllFlow(): Flow<Map<Pair<String, LocalDate>, ChildTodayRoutine>>

    fun getOverride(
        childName: String,
        date: LocalDate
    ): Flow<ChildTodayRoutine?>

    suspend fun saveOverride(
        childId: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    )

    suspend fun deleteByChildId(childId: String)
}