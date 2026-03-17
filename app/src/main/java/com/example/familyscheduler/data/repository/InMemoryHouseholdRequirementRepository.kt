package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import java.time.LocalDate

class InMemoryHouseholdRequirementRepository :
    HouseholdRequirementRepository {

    private val storage =
        mutableListOf<HouseholdRequirementRule>()

    override suspend fun getByDate(
        date: LocalDate
    ): List<HouseholdRequirementRule> {

        return storage.filter {
            it.isActiveOn(date)
        }
    }

    override suspend fun getAll(): List<HouseholdRequirementRule> {
        return storage.toList()
    }

    override suspend fun add(
        rule: HouseholdRequirementRule
    ) {
        storage.add(rule)
    }

    override suspend fun saveAll(
        rules: List<HouseholdRequirementRule>
    ) {
        storage.addAll(rules)
    }

    override suspend fun clearChildRoutineRules() {

        storage.removeAll {
            it.source == RequirementSource.CHILD_ROUTINE
        }
    }
}