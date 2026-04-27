package com.example.familyscheduler.ui.utilities

import androidx.compose.ui.graphics.Color
import com.example.familyscheduler.domain.slot.SlotState

fun slotStateColor(state: SlotState): Color =
    when (state) {
        SlotState.WORK -> Color(0xFFBBDEFB)
        SlotState.CHILDCARE -> Color(0xFFFFCDD2)
        SlotState.UNAVAILABLE -> Color(0xFFD7CCC8)
        SlotState.UNASSIGNED -> Color.LightGray
        SlotState.FREE -> Color(0xFFFFF9C4)
        SlotState.REST -> Color(0xFFC8E6C9)
        SlotState.LIFE -> Color(0xFFD1C4E9)
    }
