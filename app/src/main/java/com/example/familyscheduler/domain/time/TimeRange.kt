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

    fun contains(time: LocalTime): Boolean =
        !time.isBefore(start) && time.isBefore(end)

    fun overlaps(other: TimeRange): Boolean {
        return start < other.end && other.start < end
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