package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import java.time.LocalTime

object TemplateNormalizer {

    private val axisStart = TimeAxis.all.first()

    private val axisEnd = TimeAxis.all.last()

    fun normalize(
        schedules: List<ScheduleTemplate>
    ): List<ScheduleTemplate> {

        val result = mutableListOf<ScheduleTemplate>()

        schedules.forEach { schedule ->

            val rawStart = snapToAxis(schedule.timeRange.start)
            val rawEnd = snapToAxis(schedule.timeRange.end)

            if (rawStart == rawEnd) {
                return@forEach
            }

            if (rawStart < rawEnd) {

                // 通常
                result.add(
                    schedule.copy(
                        timeRange = TimeRange(
                            start = rawStart,
                            end = rawEnd
                        )
                    )
                )

            } else {

                // 日またぎ

                result.add(
                    schedule.copy(
                        timeRange = TimeRange(
                            start = rawStart,
                            end = nextSlot(axisEnd)
                        )
                    )
                )

                result.add(
                    schedule.copy(
                        timeRange = TimeRange(
                            start = axisStart,
                            end = rawEnd
                        )
                    )
                )
            }
        }

        return result.sortedBy { it.timeRange.start }
    }

    private fun snapToAxis(time: LocalTime): LocalTime {

        val totalMinutes = time.hour * 60 + time.minute

        val snapped =
            (totalMinutes / TimeAxis.stepMinutes) * TimeAxis.stepMinutes

        val snappedTime =
            LocalTime.of(snapped / 60, snapped % 60)

        return clamp(snappedTime)
    }

    private fun clamp(time: LocalTime): LocalTime =
        when {
            time < axisStart -> axisStart
            time > axisEnd -> axisEnd
            else -> time
        }

    private fun nextSlot(time: LocalTime): LocalTime {

        val idx = TimeAxis.indexOf(time)

        return if (idx == -1 || idx == TimeAxis.all.lastIndex)
            LocalTime.MIDNIGHT
        else
            TimeAxis.all[idx + 1]
    }
}