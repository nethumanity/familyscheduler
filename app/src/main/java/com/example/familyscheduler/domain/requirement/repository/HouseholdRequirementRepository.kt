package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HouseholdRequirementRepository {

    fun getAllFlow(): Flow<List<HouseholdRequirementRule>>

    fun getByDate(
        date: LocalDate
    ): Flow<List<HouseholdRequirementRule>>

    fun getById(
        id: String
    ): Flow<HouseholdRequirementRule?>

    suspend fun save(
        rule: HouseholdRequirementRule
    )

    suspend fun delete(id: String)
}