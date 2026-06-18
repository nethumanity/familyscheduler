package com.example.familyscheduler.domain.time

import java.time.Duration
import java.time.LocalTime

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime
) {

    init {
        require(start != end)
    }

    companion object {

        fun createOrNull(
            start: LocalTime,
            end: LocalTime
        ): TimeRange? {
            if (start == end) return null
            return TimeRange(start, end)
        }
    }

    fun overlaps(other: TimeRange): Boolean {

        val startA = TimeAxis.indexOf(start)

        val endA =
            if (end == LocalTime.MIDNIGHT)
                TimeAxis.all.size
            else
                TimeAxis.indexOf(end)

        val startB = TimeAxis.indexOf(other.start)

        val endB =
            if (other.end == LocalTime.MIDNIGHT)
                TimeAxis.all.size
            else
                TimeAxis.indexOf(other.end)

        return startA < endB && startB < endA
    }

    fun durationMinutes(): Long {
        var result =
            Duration.between(start, end).toMinutes()

        if (result < 0) {
            result += 24 * 60
        }

        return result
    }
}