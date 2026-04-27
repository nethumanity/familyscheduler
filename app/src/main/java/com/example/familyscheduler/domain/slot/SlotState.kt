package com.example.familyscheduler.domain.slot

enum class SlotState(val weight: Int) {
    WORK(5),
    CHILDCARE(4),
    FREE(1),
    REST(2),
    LIFE(3),
    UNAVAILABLE(6),
    UNASSIGNED(0);

    companion object {

        val taskInputAllowedState = listOf(
            CHILDCARE,
            WORK,
            LIFE
        )

        val totalSectionAllowedState = listOf(
            WORK,
            CHILDCARE,
            LIFE
        )

        val legendSectionAllowedState = listOf(
            WORK,
            UNAVAILABLE,
            REST,
            CHILDCARE,
            LIFE,
            FREE
        )

        val selectionSheetAllowedState = listOf(
            UNASSIGNED,
            CHILDCARE,
            LIFE,
            WORK,
            UNAVAILABLE,
            REST,
            FREE
        )
    }
}