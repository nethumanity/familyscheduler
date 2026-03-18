package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.slot.SlotState

fun slotStateLabel(state: SlotState): String =
    when (state) {
        SlotState.WORK -> "仕事"
        SlotState.CHILDCARE -> "育児"
        SlotState.FREE -> "趣味・自由"
        SlotState.REST -> "睡眠・休息"
        SlotState. LIFE -> "家事・用事"
        SlotState.UNAVAILABLE -> "移動など拘束"
        SlotState.UNASSIGNED -> "自動で割り当てる"
    }

fun labelForInput(state: SlotState): String =
    when (state) {
        SlotState.WORK -> "仕事の予定"
        SlotState.CHILDCARE -> "子どもと一緒"
        SlotState. LIFE -> "その他の家事・用事"
        else -> ""
    }