package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.slot.SlotState

enum class StateCategory {
    WORK,
    REST,
    BLOCKED;

    fun toSlotState(): SlotState =
        when (this) {
            WORK -> SlotState.WORK
            REST -> SlotState.REST
            BLOCKED -> SlotState.UNAVAILABLE
        }
}