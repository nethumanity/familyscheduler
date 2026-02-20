package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import java.time.LocalDate

class InMemoryHouseholdRequirementRepository :
    HouseholdRequirementRepository {

    private val storage =
        mutableMapOf<LocalDate, MutableList<HouseholdRequirement>>()

    override suspend fun getByDate(
        date: LocalDate
    ): List<HouseholdRequirement> {
        return storage[date]?.toList() ?: emptyList()
    }

    override suspend fun saveForDate(
        date: LocalDate,
        requirements: List<HouseholdRequirement>
    ) {
        storage[date] = requirements.toMutableList()
    }
}
