package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.viewmodel.TimelineViewModel

fun List<HouseholdRequirementRule>.toUiModels(
    requirements: List<HouseholdRequirement>,
    overrides: List<RequirementOverride>,
    viewModel: TimelineViewModel
): List<RequirementUiModel> {

    return map { rule ->
        val mode = viewModel.resolveMode(rule.id, overrides)

        val req = requirements
            .filterIsInstance<TimeRangeHouseholdRequirement>()
            .find { it.sourceRuleId == rule.id }

        RequirementUiModel(
            id = rule.id,
            name = rule.taskName,
            startIndex = req?.startIndex ?: TimeAxis.indexOf(rule.timeRange.start),
            targetState = rule.targetState,
            mode = mode
        )
    }
}