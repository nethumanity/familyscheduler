package com.example.familyscheduler.domain.routine.repository

import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RoutineShiftOverrideRepository {

    fun getAllFlow(): Flow<List <RoutineShiftOverride>>

    fun getByDate(date: LocalDate): Flow<List<RoutineShiftOverride>>

    suspend fun replace(override: RoutineShiftOverride)

    suspend fun deleteAllByChildId(childId: String)

    suspend fun delete(override: RoutineShiftOverride)
}