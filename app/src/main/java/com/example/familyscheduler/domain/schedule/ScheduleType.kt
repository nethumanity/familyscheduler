package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.slot.SlotState

enum class ScheduleType(

    val title: String,

    val state: SlotState,

    val flexWindow: Int

) {

    WORK(
        "仕事",
        SlotState.WORK,
        3
    ),

    COMMUTE_GO(
        "往路通勤",
        SlotState.UNAVAILABLE,
        3
    ),

    COMMUTE_BACK(
        "復路通勤",
        SlotState.UNAVAILABLE,
        3
    ),

    SLEEP(
        "睡眠",
        SlotState.REST,
        3
    ),

    RESTRAINT(
        "その他拘束",
        SlotState.UNAVAILABLE,
        3
    ),

    REST(
        "睡眠・休息",
        SlotState.REST,
        3
    ),

    FREE(
        "趣味・自由",
        SlotState.FREE,
        3
    );

    companion object {

        val core = listOf(
            WORK,
            COMMUTE_GO,
            COMMUTE_BACK,
            SLEEP
        )

        val additionalAllowedTypes = listOf(
            WORK,
            REST,
            FREE,
            RESTRAINT
        )
    }
}