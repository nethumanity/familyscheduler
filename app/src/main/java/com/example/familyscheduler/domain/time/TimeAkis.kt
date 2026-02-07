package com.example.familyscheduler.domain.time

import java.time.LocalTime

object TimeAxis {

    private const val START_MIN = 5 * 60
    private const val END_MIN = 23 * 60 + 30
    private const val STEP = 30

    val times: List<LocalTime> =
        (START_MIN..END_MIN step STEP).map { min ->
            LocalTime.of(min / 60, min % 60)
        }

    fun indexOf(time: LocalTime): Int =
        times.indexOf(time)

    fun timeAt(index: Int): LocalTime =
        times[index]

    val size: Int
        get() = times.size

    val indices = 0 until size

    const val stepMinutes = STEP
}
