package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.interaction.BlockAction
import com.example.familyscheduler.domain.interaction.TimelineBlock
import com.example.familyscheduler.domain.requirement.RequirementSemantics
import com.example.familyscheduler.domain.requirement.RequirementShiftOverride
import com.example.familyscheduler.domain.routine.ChildCareEvent
import com.example.familyscheduler.domain.routine.ChildCareLabel
import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.projection.StatusUiModel.Canceled.toStatusUiModel

fun TimelineBlock.toRequirementUiModel(
    name: String,
    requirementShiftMap: Map<String, RequirementShiftOverride>,
    routineShiftByIdEvent: Map<Pair<String, ChildCareLabel>, RoutineShiftOverride>,
    childCareEventMap: Map<String, ChildCareEvent>
): RequirementUiModel {

    val requirementId = requirementIds.first()

    val startText = TimeAxis.timeLabelAt(startIndex)

    val isProposalApplied =
        when (semantics) {
            RequirementSemantics.TASK -> {
                requirementShiftMap[requirementId] != null
            }
            RequirementSemantics.EVENT -> {
                val event = childCareEventMap[requirementId]

                // 1人でもoverrideならtrue
                event?.childIds
                    ?.any { childId ->
                        routineShiftByIdEvent[childId to event.label] != null
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
