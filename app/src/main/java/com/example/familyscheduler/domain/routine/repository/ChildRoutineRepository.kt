package com.example.familyscheduler.domain.routine.repository

import com.example.familyscheduler.domain.routine.ChildRoutineInput

interface ChildRoutineRepository {

    suspend fun add(input: ChildRoutineInput)

    suspend fun getAll(): List<ChildRoutineInput>

    suspend fun getFromChildName(childName: String): ChildRoutineInput?

    suspend fun delete(name: String)
}