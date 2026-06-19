package com.example.familyscheduler.domain.requirement

import java.time.LocalDate

sealed interface RequirementOverride {
    val ruleId: String
    val date: LocalDate
    val type: RequirementOverrideType
}

data class RequirementToggleOverride(
    override val ruleId: String,
    override val date: LocalDate,
    val mode: RequirementModeToday
) : RequirementOverride {
    override val type = RequirementOverrideType.TOGGLE
}

data class RequirementShiftOverride(
    override val ruleId: String,
    override val date: LocalDate,
    val deltaSteps: Int
) : RequirementOverride {
    override val type = RequirementOverrideType.SHIFT
}