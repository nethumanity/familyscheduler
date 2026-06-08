package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.interaction.BlockAction
import com.example.familyscheduler.domain.interaction.TimelineBlock
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.projection.StatusUiModel.Canceled.toStatusUiModel

fun TimelineBlock.toCareStateUiModel(): CareStateUiModel {

    val timeText = TimeAxis.timeLabelRange(startIndex, endIndex)

    return CareStateUiModel(
        requirementIds = requirementIds,
        timeText = timeText,
        mode = mode,
        status = toStatusUiModel(),
        soloApplicable = BlockAction.SOLO in allowedActions,
        reverseAssignable = BlockAction.REVERSE in allowedActions
    )
}