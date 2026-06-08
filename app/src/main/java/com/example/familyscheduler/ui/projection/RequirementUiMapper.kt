package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.interaction.BlockAction
import com.example.familyscheduler.domain.interaction.TimelineBlock
import com.example.familyscheduler.domain.requirement.RequirementSemantics
import com.example.familyscheduler.domain.requirement.RequirementShiftOverride
import com.example.familyscheduler.domain.routine.ChildCareEvent
import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.projection.StatusUiModel.Canceled.toStatusUiModel

fun TimelineBlock.toRequirementUiModel(
    name: String,
    requirementShiftOverrides: List<RequirementShiftOverride>,
    routineShiftOverrides: List<RoutineShiftOverride>,
    events: List<ChildCareEvent>
): RequirementUiModel {

    val requirementId = requirementIds.first()

    val startText = TimeAxis.timeLabelAt(startIndex)

    val isProposalApplied =
        when (semantics) {
            RequirementSemantics.TASK -> {
                requirementShiftOverrides
                    .any {
                        it.ruleId == requirementId
                    }
            }
            RequirementSemantics.EVENT -> {
                val event = events
                    .firstOrNull { it.eventId == requirementId }
                event?.let {
                    routineShiftOverrides
                        .any {
                            it.childId in event.childIds && // 1人でもoverrideならtrue
                                    it.eventType == event.label
                        }
                } ?: false
            }
            else -> false
        }

    return RequirementUiModel(
        requirementId = requirementId,
        startText = startText,
        nameText = name,
        mode = mode,
        status = toStatusUiModel(),
        isProposalApplied = isProposalApplied,
        canEdit = BlockAction.EDIT in allowedActions,
        cancelApplicable = BlockAction.CANCEL in allowedActions,
        soloApplicable = BlockAction.SOLO in allowedActions,
        reverseAssignable = BlockAction.REVERSE in allowedActions
    )
}
