package com.example.familyscheduler.ui.components

import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.TimeRange
import java.time.LocalTime

object TemplateNormalizer {

    private val axisStart =
        LocalTime.MIDNIGHT

    private val axisEndExclusive =
        LocalTime.MIDNIGHT

    fun normalize(
        schedules: List<ScheduleTemplate>
    ): List<ScheduleTemplate> {

        val result =
            mutableListOf<ScheduleTemplate>()

        schedules.forEach { schedule ->

            val start =
                clampToAxis(schedule.timeRange.start)

            val end =
                clampToAxis(schedule.timeRange.end)

            when {

                start == end -> {
                    // 無効
                }

                start < end -> {

                    result.add(
                        schedule.copy(
                            timeRange =
                                TimeRange(start, end)
                        )
                    )
                }

                start > end -> {

                    // start → 24:00
                    result.add(
                        schedule.copy(
                            timeRange =
                                TimeRange(
                                    start,
                                    LocalTime.MIDNIGHT
                                )
                        )
                    )

                    // 00:00 → end
                    result.add(
                        schedule.copy(
                            timeRange =
                                TimeRange(
                                    axisStart,
                                    end
                                )
                        )
                    )
                }
            }
        }

        return result.sortedBy {
            it.timeRange.start
        }
    }

    private fun clampToAxis(
        time: LocalTime
    ): LocalTime {

        return when {

            time < axisStart ->
                axisStart

            else ->
                time
        }
    }
}
