package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.RequirementOverride
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RequirementOverrideRepository {

    fun getAllFlow(): Flow<List<RequirementOverride>>

    fun getOverrides(date: LocalDate): Flow<List<RequirementOverride>>

    fun getOverrides(ruleId: String, date: LocalDate): Flow<List<RequirementOverride>>

    suspend fun saveOverride(override: RequirementOverride)

    suspend fun deleteByRuleId(ruleId: String)
}