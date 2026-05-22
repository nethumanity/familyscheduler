package com.example.familyscheduler.ui.mapper

import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.RequirementShiftOverride
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.routine.ChildCareEvent
import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.utilities.RequirementUiModel
import com.example.familyscheduler.viewmodel.TimelineViewModel
import java.time.LocalDate

fun List<HouseholdRequirementRule>.toUiModels(
    requirements: List<HouseholdRequirement>,
    overrides: List<RequirementOverride>,
    shiftOverrides: List<RoutineShiftOverride>,
    events: List<ChildCareEvent>,
    date: LocalDate,
    viewModel: TimelineViewModel
): List<RequirementUiModel> {

    return map { rule ->
        val mode = viewModel.resolveMode(rule.id, overrides)

        val req = requirements
            .filterIsInstance<TimeRangeHouseholdRequirement>()
            .find { it.sourceRuleId == rule.id }

        val canEdit = (rule.source == RequirementSource.USER)

        val isProposalApplied =
            when (rule.source) {

                RequirementSource.USER -> {
                    overrides
                        .filterIsInstance<RequirementShiftOverride>()
                        .any {
                            it.ruleId == rule.id &&
                                    it.date == date
                                    //&& it.isFromProposal
                        }
                }

                RequirementSource.NURSERY_DROP_OFF, RequirementSource.NURSERY_PICKUP -> {
                    val event = events
                        .firstOrNull { it.eventId == rule.id }
                    event?.let {
                        shiftOverrides
                            .any {
                                it.childId in event.childIds && // 1人でもoverrideならtrue
                                        it.date == date &&
                                        it.eventType == event.label
                                        //&& it.isFromProposal
                            }
                    } ?: false
                }

                else -> false
            }


        RequirementUiModel(
            id = rule.id,
            name = rule.taskName,
            startIndex = req?.startIndex ?: TimeAxis.indexOf(rule.timeRange.start),
            targetState = rule.targetState,
            mode = mode,
            canEdit = canEdit,
            isProposalApplied = isProposalApplied
        )
    }
}