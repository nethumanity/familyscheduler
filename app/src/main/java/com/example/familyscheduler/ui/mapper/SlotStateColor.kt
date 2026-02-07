package com.example.familyscheduler.ui.mapper

import androidx.compose.ui.graphics.Color
import com.example.familyscheduler.domain.model.SlotState

fun slotStateColor(state: SlotState): Color =
    when (state) {
        SlotState.WORK -> Color(0xFFBBDEFB)        // 青
        SlotState.CHILDCARE -> Color(0xFFFFCDD2)  // 赤
        SlotState.UNAVAILABLE -> Color.LightGray
        SlotState.UNASSIGNED -> Color.LightGray
        SlotState.FREE -> Color(0xFFFFF9C4)       // 黄
        SlotState.REST -> Color(0xFFC8E6C9)        // 緑
        SlotState.LIFE -> Color(0xFFC8E6C9)        // 緑
    }
