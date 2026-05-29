package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.interaction.BlockAction
import com.example.familyscheduler.domain.interaction.TimelineBlock
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.projection.StatusUiModel.Canceled.toStatusUiModel

fun TimelineBlock.toCareStateUiModel(): CareStateUiModel {

    fun indexToTime(index: Int) = TimeAxis.all.getOrNull(index)

    val timeText = "${indexToTime(startIndex)}–${indexToTime(endIndex)}"

    return CareStateUiModel(
        requirementIds = requirementIds,
        timeText = timeText,
        mode = mode,
        status = toStatusUiModel(),
        soloApplicable = BlockAction.SOLO in allowedActions,
        reverseAssignable = BlockAction.REVERSE in allowedActions
    )
}