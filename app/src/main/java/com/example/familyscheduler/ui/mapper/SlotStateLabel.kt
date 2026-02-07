package com.example.familyscheduler.ui.mapper

import com.example.familyscheduler.domain.model.SlotState


fun slotStateLabel(state: SlotState): String =
    when (state) {
        SlotState.WORK -> "仕事"
        SlotState.CHILDCARE -> "育児"
        SlotState.FREE -> "自由時間"
        SlotState.REST -> "睡眠"
        SlotState. LIFE -> "家事・用事・食事"
        SlotState.UNAVAILABLE -> "仕事の準備・移動"
        SlotState.UNASSIGNED -> "自動で割り当てる"
    }