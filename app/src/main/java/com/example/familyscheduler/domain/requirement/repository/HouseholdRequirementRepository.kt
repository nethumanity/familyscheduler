package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import java.time.LocalDate

interface HouseholdRequirementRepository {

    suspend fun getByDate(
        date: LocalDate
    ): List<HouseholdRequirementRule>

    suspend fun add(
        rule: HouseholdRequirementRule
    )
}