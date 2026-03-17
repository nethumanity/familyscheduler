package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import java.time.LocalDate

interface HouseholdRequirementRepository {

    suspend fun getByDate(
        date: LocalDate
    ): List<HouseholdRequirementRule>

    suspend fun getAll(): List<HouseholdRequirementRule>

    suspend fun add(
        rule: HouseholdRequirementRule
    )

    suspend fun saveAll(
        rules: List<HouseholdRequirementRule>
    )

    suspend fun clearChildRoutineRules()
}