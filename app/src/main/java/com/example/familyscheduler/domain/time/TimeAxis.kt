package com.example.familyscheduler.domain.time

import java.time.LocalTime

object TimeAxis {

    private const val START_MIN = 0 * 60
    private const val END_MIN = 24 * 60
    private const val SLOT_MINUTES = 30

    val all: List<LocalTime> =
        (START_MIN until END_MIN step SLOT_MINUTES).map { min ->
            LocalTime.of(min / 60, min % 60)
        }

    fun indexOf(time: LocalTime): Int {
        val index = all.indexOf(time)
        require(index >= 0) {
            "TimeAxis does not contain the given time: $time. " +
                    "Expected multiples of $SLOT_MINUTES minutes."
        }
        return index
    }

    fun distance(
        from: LocalTime,
        to: LocalTime
    ): Int {
        return indexOf(to) - indexOf(from)
    }

    val indices: IntRange
        get() = all.indices

    const val stepMinutes = SLOT_MINUTES

    // UI表示用
//    val displayStartIndex =
//        indexOf(LocalTime.of(5, 0))
//
//    val displayEndIndex =
//        indexOf(LocalTime.of(23, 30))

    fun timeLabelAt(index: Int): String =
        when (index) {
            all.size -> "24:00"
            else -> all.getOrNull(index)?.toString() ?: "--:--"
        }

    fun timeLabelRange(
        startIndex: Int,
        endIndex: Int
    ): String =
        "${timeLabelAt(startIndex)}–${timeLabelAt(endIndex)}"
}
