package com.example.familyscheduler.domain.schedule

import java.time.LocalTime

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime
) {
    init {
        require(start < end)    //「睡眠」はstart>endになるので、関数を作って処理が必要？
    }

    fun contains(time: LocalTime): Boolean =
        !time.isBefore(start) && time.isBefore(end)

    fun overlaps(other: TimeRange): Boolean {
        return start < other.end && other.start < end
    }
}
