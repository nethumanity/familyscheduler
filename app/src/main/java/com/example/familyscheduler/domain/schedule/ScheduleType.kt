package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.slot.SlotState

enum class ScheduleType(
    val title: String,
    val state: SlotState,
    val priority: Int
) {

    WORK("仕事", SlotState.WORK, 60),
    COMMUTE_GO("往路通勤", SlotState.UNAVAILABLE, 90),
    COMMUTE_BACK("復路通勤", SlotState.UNAVAILABLE, 80),
    SLEEP("睡眠", SlotState.REST, 30),
    ADDITIONAL_WORK("仕事", SlotState.WORK, 70),
    RESTRAINT("その他拘束", SlotState.UNAVAILABLE, 100),
    REST("睡眠・休息", SlotState.REST, 40),
    FREE("趣味・自由", SlotState.FREE, 10);

    companion object {

        val core = listOf(
            WORK,
            COMMUTE_GO,
            COMMUTE_BACK,
            SLEEP
        )

        val additionalAllowedTypes = listOf(
            ADDITIONAL_WORK,
            REST,
            FREE,
            RESTRAINT
        )
    }
}