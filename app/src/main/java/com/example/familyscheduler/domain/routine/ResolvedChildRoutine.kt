package com.example.familyscheduler.domain.routine

import java.time.LocalTime

data class ResolvedChildRoutine(
    val childId: String,
    val childName: String,
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