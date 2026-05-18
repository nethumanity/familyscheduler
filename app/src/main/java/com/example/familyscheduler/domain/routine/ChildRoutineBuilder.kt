package com.example.familyscheduler.domain.routine

import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalDate
import java.time.LocalTime

class ChildRoutineBuilder {

    fun build(
        date: LocalDate,
        routines: List<ResolvedChildRoutine>
    ): RoutineResult {

        val blocks = buildBlocks(routines, date)

        val dropEvents = buildEvents(
            routines = routines,
            date = date,
            timeSelector = { it.nurseryStart },
            earliestSelector = { it.nurseryStartEarliest },
            latestSelector = { it.nurseryStartLatest },
            label = ChildCareLabel.NURSERY_DROP_OFF,
            idPrefix = "DROP"
        )

        val pickupEvents = buildEvents(
            routines = routines,
            date = date,
            timeSelector = { it.nurseryEnd },
            earliestSelector = { it.nurseryEndEarliest },
            latestSelector = { it.nurseryEndLatest },
            label = ChildCareLabel.NURSERY_PICKUP,
            idPrefix = "PICKUP"
        )

        val events = dropEvents + pickupEvents

        return RoutineResult(
            blocks = blocks + events.toBlocks(date),
            events = events
        )
    }

    private fun buildBlocks(
        routines: List<ResolvedChildRoutine>,
        date: LocalDate
    ): List<ChildCareBlock> {

        return TimeAxis.all.mapNotNull { start ->

            val end = start.plusMinutes(TimeAxis.stepMinutes.toLong())

            val activeChildren = routines.count { child ->

                if (child.todayRoutine == ChildTodayRoutine.NONE) {
                    return@count false
                }

                start >= child.wakeUpTime &&
                        start <= child.sleepTime &&                 // 拡張要素：寝かしつけのイベント化 → start < child.sleepTimeに修正
                        (
                                child.todayRoutine == ChildTodayRoutine.HOME ||
                                        start < child.nurseryStart ||
                                        start > child.nurseryEnd    // 拡張要素：child.nurseryEnd + (dropOffSteps - 1) * TimeAxis.stepMinutes
                                )
            }

            if (activeChildren == 0) {
                return@mapNotNull null
            }

            ChildCareBlock(
                id = "CHILDCARE_${date}_${start}",
                date = date,
                startTime = start,
                endTime = end,
                activeChildrenCount = activeChildren
            )
        }
    }

    private fun buildEvents(
        routines: List<ResolvedChildRoutine>,
        date: LocalDate,
        timeSelector: (ResolvedChildRoutine) -> LocalTime,
        earliestSelector: (ResolvedChildRoutine) -> LocalTime,
        latestSelector: (ResolvedChildRoutine) -> LocalTime,
        label: ChildCareLabel,
        idPrefix: String
    ): List<ChildCareEvent> {

        return routines
            .filter { it.todayRoutine == ChildTodayRoutine.NURSERY }
            .groupBy(timeSelector)
            .map { (time, group) ->

                ChildCareEvent(
                    eventId = "${idPrefix}_${date}_$time",
                    time = time,
                    duration = TimeAxis.stepMinutes,    // 拡張要素：dropOffSteps(or pickupSteps) * TimeAxis.stepMinutes
                    label = label,
                    flexEarliest = group.maxOf(earliestSelector),
                    flexLatest = group.minOf(latestSelector),
                    childIds = group.map { it.childId }
                )
            }
    }

    private fun List<ChildCareEvent>.toBlocks(
        date: LocalDate
    ): List<ChildCareBlock> {

        return map { event ->

            ChildCareBlock(
                id = event.eventId,
                date = date,
                startTime = event.time,
                endTime = event.time.plusMinutes(event.duration.toLong()),
                label = event.label,
                flexEarliest = event.flexEarliest,
                flexLatest = event.flexLatest,
                activeChildrenCount = event.childIds.size
            )
        }
    }
}