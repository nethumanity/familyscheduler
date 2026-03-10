package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.ChildRoutineInput

interface ChildRoutineRepository {

    suspend fun add(input: ChildRoutineInput)

    suspend fun getAll(): List<ChildRoutineInput>

    suspend fun delete(name: String)
}