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

    override suspend fun getFromId(
        id: String
    ): HouseholdRequirementRule? {

        return storage.firstOrNull { it.id == id }
    }

    override suspend fun getAll(): List<HouseholdRequirementRule> {
        return storage.toList()
    }

    override suspend fun add(
        rule: HouseholdRequirementRule
    ) {
        val index = storage.indexOfFirst { it.id == rule.id }

        if (index >= 0) {
            // 更新
            storage[index] = rule
        } else {
            // 新規追加
            storage.add(rule)
        }
    }

    override suspend fun saveAll(
        rules: List<HouseholdRequirementRule>
    ) {
        storage.addAll(rules)
    }

    override suspend fun delete(id: String) {
        storage.removeAll { it.id == id }
    }

    override suspend fun clearChildRoutineRules() {

        storage.removeAll {
            it.source == RequirementSource.CHILD_ROUTINE
        }
    }
}