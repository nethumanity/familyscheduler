package com.example.familyscheduler.domain.template

import java.time.LocalTime

data class TimeWindow(
    val start: LocalTime,
    val end: LocalTime
)