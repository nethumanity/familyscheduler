package com.example.familyscheduler.domain.time

import java.time.LocalTime

object TimeAxis {

    private const val START_MIN = 5 * 60        //0 * 60 にする可能性あり
    private const val END_MIN = 23 * 60 + 30    //24 * 60 にする可能性あり
    private const val SLOT_MINUTES = 30

    val all: List<LocalTime> =
        (START_MIN..END_MIN step SLOT_MINUTES).map { min ->
            LocalTime.of(min / 60, min % 60)
        }

    fun indexOf(time: LocalTime): Int =
        all.indexOf(time)

    val indices: IntRange
        get() = all.indices

    const val stepMinutes = SLOT_MINUTES
}
