package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.repository.RequirementOverrideRepository
import java.time.LocalDate

class InMemoryRequirementOverrideRepository : RequirementOverrideRepository {

    private val overrides = mutableListOf<RequirementOverride>()

    override fun getOverrides(date: LocalDate): List<RequirementOverride> {
        return overrides.filter { it.date == date }
    }

    override fun getOverrides(ruleId: String, date: LocalDate): List<RequirementOverride> {
        return overrides.filter { it.ruleId == ruleId && it.date == date }
    }

    override fun saveOverride(override: RequirementOverride) {
        // 同種overrideは置き換え
        overrides.removeIf {
            it.ruleId == override.ruleId &&
                    it.date == override.date &&
                    it::class == override::class
        }
        overrides.add(override)
    }

    override fun getAll(): List<RequirementOverride> {
        return overrides.toList()
    }

    override suspend fun deleteByRuleId(ruleId: String) {
        overrides.removeAll { it.ruleId == ruleId }
    }
}