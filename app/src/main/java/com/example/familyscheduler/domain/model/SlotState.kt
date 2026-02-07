package com.example.familyscheduler.domain.model

enum class SlotState(val weight: Int) {
    WORK(0),
    CHILDCARE(1),
    FREE(4),
    REST(3),
    LIFE(2),
    UNAVAILABLE(0),
    UNASSIGNED(0)
}

