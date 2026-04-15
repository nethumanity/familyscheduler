package com.example.familyscheduler.domain.routine

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

data class ChildRoutineInput(
    val childId: String = UUID.randomUUID().toString(),
    val childName: String,
    val wakeUpTime: LocalTime,
    val sleepTime: LocalTime,
    val daysOfWeek: Set<DayOfWeek>,         // 空のsetも含めて必須項目
    val nurseryStart: LocalTime,
    val nurseryStartEarliest: LocalTime,    // 初期値はnurseryStart
    val nurseryStartLatest: LocalTime,      // 初期値はnurseryStart
    val nurseryEnd: LocalTime,
    val nurseryEndEarliest: LocalTime,      // 初期値はnurseryEnd
    val nurseryEndLatest: LocalTime         // 初期値はnurseryEnd
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