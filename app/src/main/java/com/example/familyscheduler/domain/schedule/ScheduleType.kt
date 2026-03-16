package com.example.familyscheduler.domain.schedule

data class ScheduleType(
    val title: String,
    val category: StateCategory,
    val flexWindow: Int
)

object ScheduleTypes {

    val WORK = ScheduleType("仕事", StateCategory.WORK, 3)
    val COMMUTE_GO = ScheduleType("往路通勤", StateCategory.BLOCKED, 3)
    val COMMUTE_BACK = ScheduleType("復路通勤", StateCategory.BLOCKED, 3)
    val SLEEP = ScheduleType("睡眠", StateCategory.REST, 3)

    val core = listOf(
        WORK,
        COMMUTE_GO,
        COMMUTE_BACK,
        SLEEP
    )
}
