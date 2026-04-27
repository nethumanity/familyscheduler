package com.example.familyscheduler.domain.routine

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

data class ChildRoutineInput(
    val childId: String = UUID.randomUUID().toString(),
    val childName: String,
    val wakeUpTime: LocalTime,
    val sleepTime: LocalTime,
    val daysOfWeek: Set<DayOfWeek>,
    val nurseryStart: LocalTime,
    val nurseryStartEarliest: LocalTime,
    val nurseryStartLatest: LocalTime,
    val nurseryEnd: LocalTime,
    val nurseryEndEarliest: LocalTime,
    val nurseryEndLatest: LocalTime
) {

    init {
        require(wakeUpTime < sleepTime)
        require(nurseryStart <= nurseryEnd)
        require(nurseryStartEarliest <= nurseryStart)
        require(nurseryStart <= nurseryStartLatest)
        require(nurseryEndEarliest <= nurseryEnd)
        require(nurseryEnd <= nurseryEndLatest)
    }

}