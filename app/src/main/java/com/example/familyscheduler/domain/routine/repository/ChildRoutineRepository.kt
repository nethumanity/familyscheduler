package com.example.familyscheduler.domain.routine.repository

import com.example.familyscheduler.domain.routine.ChildRoutineInput
import kotlinx.coroutines.flow.Flow

interface ChildRoutineRepository {

    fun getAllFlow(): Flow<List<ChildRoutineInput>>

    fun getByChildName(childName: String): Flow<ChildRoutineInput?>

    suspend fun save(input: ChildRoutineInput)

    suspend fun delete(name: String)
}