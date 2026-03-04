package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import java.time.LocalDate

class InMemoryHouseholdRequirementRepository :
    HouseholdRequirementRepository {

    private val storage =
        mutableMapOf<LocalDate, MutableList<HouseholdRequirementRule>>()

    override suspend fun getByDate(
        date: LocalDate
    ): List<HouseholdRequirementRule> {
        return storage[date]?.toList() ?: emptyList()
    }

    override suspend fun saveForDate(
        date: LocalDate,
        requirements: List<HouseholdRequirementRule>
    ) {
        storage[date] = requirements.toMutableList()
    }
}