package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.requirement.ChildRoutineInput
import com.example.familyscheduler.domain.requirement.repository.ChildRoutineRepository

class InMemoryChildRoutineRepository :
    ChildRoutineRepository {

    private val routines =
        mutableListOf<ChildRoutineInput>()

    override suspend fun add(input: ChildRoutineInput) {
        routines.removeAll { it.name == input.name }
        routines.add(input)
    }

    override suspend fun getAll(): List<ChildRoutineInput> {
        return routines.toList()
    }

    override suspend fun delete(name: String) {
        routines.removeAll { it.name == name }
    }
}