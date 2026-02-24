package com.example.familyscheduler.ui.components

import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.TimeRange
import java.time.LocalTime

object TemplateNormalizer {

    /**
     * ScheduleTemplate を正規化する
     *
     * 正規化内容：
     * ・start == end → 削除（無効）
     * ・start < end → そのまま
     * ・start > end → 日跨ぎ → 2つに分割
     */
    fun normalize(
        schedules: List<ScheduleTemplate>
    ): List<ScheduleTemplate> {

        val result = mutableListOf<ScheduleTemplate>()

        schedules.forEach { schedule ->

            val start = schedule.timeRange.start
            val end = schedule.timeRange.end

            when {

                // 無効
                start == end -> {
                    // 何もしない
                }

                // 通常
                start < end -> {
                    result.add(schedule)
                }

                // 日跨ぎ（例：22:00 → 06:00）
                start > end -> {

                    result.add(
                        schedule.copy(
                            timeRange = TimeRange(
                                start = start,
                                end = LocalTime.MAX
                            )
                        )
                    )

                    result.add(
                        schedule.copy(
                            timeRange = TimeRange(
                                start = LocalTime.MIN,
                                end = end
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
}