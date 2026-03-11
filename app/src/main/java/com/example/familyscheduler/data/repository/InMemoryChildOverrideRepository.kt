package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.requirement.ChildTodayRoutine
import com.example.familyscheduler.domain.requirement.repository.ChildOverrideRepository
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
}