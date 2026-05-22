package com.example.familyscheduler.ui.presentation

import androidx.compose.ui.graphics.Color
import com.example.familyscheduler.domain.slot.SlotState

object SlotStatePresentation {

    fun label(state: SlotState): String =
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

    fun color(state: SlotState): Color =
        when (state) {
            SlotState.WORK -> Color(0xFFBBDEFB)
            SlotState.CHILDCARE -> Color(0xFFFFCDD2)
            SlotState.UNAVAILABLE -> Color(0xFFD7CCC8)
            SlotState.UNASSIGNED -> Color.LightGray
            SlotState.FREE -> Color(0xFFFFF9C4)
            SlotState.REST -> Color(0xFFC8E6C9)
            SlotState.LIFE -> Color(0xFFD1C4E9)
        }

}