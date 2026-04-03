package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class InMemoryHouseholdRequirementRepository : HouseholdRequirementRepository {

    private val _rules = MutableStateFlow<List<HouseholdRequirementRule>>(emptyList())

    override fun getAllFlow(): Flow<List<HouseholdRequirementRule>> {
        return _rules
    }

    // いらない？
    override fun getByDate(
        date: LocalDate
    ): Flow<List<HouseholdRequirementRule>> {
        return _rules
            .map { list ->
                list.filter { rule ->
                    rule.isActiveOn(date)
                }
            }
    }

    // 編集画面用（いらない？）
    override fun getById(
        id: String
    ): Flow<HouseholdRequirementRule?> {
        return _rules
            .map { list ->
                list.firstOrNull { it.id == id }
            }
    }

    override suspend fun save(
        rule: HouseholdRequirementRule
    ) {
        _rules.update { old ->
            val filtered = old.filterNot { it.id == rule.id }

            filtered + rule
        }
    }

    override suspend fun delete(id: String) {
        _rules.update { old ->
            val filtered = old.filterNot {
                it.id == id
            }
            filtered
        }
    }
}