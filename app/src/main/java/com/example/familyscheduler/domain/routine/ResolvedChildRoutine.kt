package com.example.familyscheduler.domain.routine

import java.time.LocalTime

data class ResolvedChildRoutine(
    val name: String,
    val wakeUpTime: LocalTime,
    val sleepTime: LocalTime,
    val todayRoutine: ChildTodayRoutine,
    val nurseryStart: LocalTime,
    val nurseryEnd: LocalTime,
    val nurseryStartEarliest: LocalTime,
    val nurseryStartLatest: LocalTime,
    val nurseryEndEarliest: LocalTime,
    val nurseryEndLatest: LocalTime
)