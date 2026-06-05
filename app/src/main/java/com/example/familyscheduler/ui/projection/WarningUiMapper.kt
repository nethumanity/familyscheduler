package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.interaction.BlockAction
import com.example.familyscheduler.domain.interaction.TimelineBlock
import com.example.familyscheduler.domain.requirement.RequirementSemantics
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.presentation.SlotStatePresentation

fun TimelineBlock.toWarningUiModel(
    ruleId: String,
    name: String,
    hasProposal: Boolean
): WarningUiModel {

    fun indexToTime(index: Int) = TimeAxis.all.getOrNull(index)

    val timeText = "${indexToTime(startIndex)}–${indexToTime(endIndex)}"

    val nameText = if (semantics == RequirementSemantics.STATE) {
        SlotStatePresentation.label(SlotState.CHILDCARE)
    } else {
        name
    }

    val personStates =
        PersonAvailabilityUiModel(
            blockingPersons = blockingPersons,
            requiredCount = requiredCount
        )

    return WarningUiModel(
        dialogKey = WarningDialogKey(
            index = startIndex,
            ruleId = ruleId
        ),
        requirementIds = requirementIds,
        timeText = timeText,
        nameText = nameText,
        personStates = personStates,
        hasProposal = hasProposal,
        cancelApplicable = BlockAction.CANCEL in allowedActions,
        soloApplicable = BlockAction.SOLO in allowedActions
    )
}