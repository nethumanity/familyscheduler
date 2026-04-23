package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.RequirementOverride
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RequirementOverrideRepository {

    fun getByDate(date: LocalDate): Flow<List<RequirementOverride>>

    suspend fun replace(override: RequirementOverride)

    suspend fun deleteAllByRuleId(ruleId: String)

    suspend fun delete(override: RequirementOverride)
}