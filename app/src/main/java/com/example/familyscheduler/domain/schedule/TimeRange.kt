package com.example.familyscheduler.domain.schedule

import java.time.LocalTime

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime
) {

    init {
        require(start < end)
    }

    companion object {

        fun createOrNull(
            start: LocalTime,
            end: LocalTime
        ): TimeRange? {
            return if (start < end) {
                TimeRange(start, end)
            } else {
                null
            }
        }
    }

    fun contains(time: LocalTime): Boolean =
        !time.isBefore(start) && time.isBefore(end)

    fun overlaps(other: TimeRange): Boolean {
        return start < other.end && other.start < end
    }
}