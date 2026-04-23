package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementOverride

data class RequirementUndoPayload(
    val requirement: HouseholdRequirementRule,
    val overrides: List<RequirementOverride>
)