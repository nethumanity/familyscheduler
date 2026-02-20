package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import java.time.LocalDate

interface HouseholdRequirementRepository {

    suspend fun getByDate(date: LocalDate): List<HouseholdRequirement>

    suspend fun saveForDate(
        date: LocalDate,
        requirements: List<HouseholdRequirement>
    )
}
