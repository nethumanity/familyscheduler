package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.repository.ChildOverrideRepository
import java.time.LocalDate

class InMemoryChildOverrideRepository: ChildOverrideRepository {

    private val overrides =
        mutableMapOf<Pair<String, LocalDate>, ChildTodayRoutine>()

    override fun getOverride(
        childName: String,
        date: LocalDate
    ): ChildTodayRoutine? {
        return overrides[childName to date]
    }

    override fun saveOverride(
        childName: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    ) {
        overrides[childName to date] = routine
    }

    override fun getAll(): Map<Pair<String, LocalDate>, ChildTodayRoutine> {
        return overrides.toMap()
    }

    override suspend fun deleteByChildName(childName: String) {
        overrides.entries.removeAll { it.key.first == childName }
    }
}