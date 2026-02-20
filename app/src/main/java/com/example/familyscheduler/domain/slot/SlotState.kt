package com.example.familyscheduler.domain.slot

enum class SlotState(val weight: Int) {
    WORK(5),
    CHILDCARE(4),
    FREE(1),
    REST(2),
    LIFE(3),
    UNAVAILABLE(6),
    UNASSIGNED(0)
}