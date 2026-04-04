package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.repository.RequirementOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class InMemoryRequirementOverrideRepository : RequirementOverrideRepository {

    private val _overrides = MutableStateFlow<List<RequirementOverride>>(emptyList())

    override fun getAllFlow(): Flow<List<RequirementOverride>> {
        return _overrides
    }

    //いらない？使い方は↓
    //val overridesForDate = currentDate.flatMapLatest { date -> repository.getByDate(date) }
    override fun getByDate(date: LocalDate): Flow<List<RequirementOverride>> {
        return _overrides
            .map { list ->
                list.filter { it.date == date }
            }
            .distinctUntilChanged()
    }

    //いらない？
    override fun getOverrides(ruleId: String, date: LocalDate): Flow<List<RequirementOverride>> {
        return _overrides
            .map { list ->
                list.filter { it.ruleId == ruleId && it.date == date }
            }
            .distinctUntilChanged()
    }

    override suspend fun saveOverride(override: RequirementOverride) {
        // 同種overrideは置き換え
        _overrides.update { old ->
            val filtered = old.filterNot {
                it.ruleId == override.ruleId &&
                        it.date == override.date &&
                        it::class == override::class
            }
            filtered + override
        }
    }

    override suspend fun deleteByRuleId(ruleId: String) {
        _overrides.update { old ->
            val filtered = old.filterNot {
                it.ruleId == ruleId
            }
            filtered
        }
    }
}