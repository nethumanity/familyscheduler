package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.RequirementOverride
import java.time.LocalDate

interface RequirementOverrideRepository {

    fun getOverrides(date: LocalDate): List<RequirementOverride>

    fun getOverrides(ruleId: String, date: LocalDate): List<RequirementOverride>

    fun saveOverride(override: RequirementOverride)
    fun getAll(): List<RequirementOverride>

}