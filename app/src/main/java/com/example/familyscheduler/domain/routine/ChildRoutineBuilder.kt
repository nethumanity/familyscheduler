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

        val dropEvents = buildDropEvents(routines, date)
        val pickupEvents = buildPickupEvents(routines, date)
        val events = dropEvents.values + pickupEvents.values

        return RoutineResult(blocks + events.toBlocks(date), events)
    }

    private fun buildBlocks(
        routines: List<ResolvedChildRoutine>,
        date: LocalDate
    ): List<ChildCareBlock> {

        val blocks = mutableListOf<ChildCareBlock>()

        for (start in TimeAxis.all) {

            val end = start.plusMinutes(TimeAxis.stepMinutes.toLong())

            val activeChildren = routines.count { child ->

                if (child.todayRoutine == ChildTodayRoutine.NONE) {
                    return@count false
                }

                val needsCare =
                    start >= child.wakeUpTime &&
                            start <= child.sleepTime && // 拡張要素：寝かしつけのイベント化
                            (
                                    child.todayRoutine == ChildTodayRoutine.HOME ||
                                            start < child.nurseryStart ||
                                            start > child.nurseryEnd // 拡張要素：child.nurseryEnd + (dropOffSteps - 1) * TimeAxis.stepMinutes
                                    )

                needsCare
            }

            if (activeChildren > 0) {
                blocks += ChildCareBlock(
                    eventId = "CHILDCARE_${date}_${start}",
                    daysOfWeek = setOf(date.dayOfWeek),
                    startTime = start,
                    endTime = end,
                    activeChildrenCount = activeChildren
                )
            }
        }

        return blocks
    }

    private fun buildDropEvents(
        routines: List<ResolvedChildRoutine>,
        date: LocalDate
    ): Map<LocalTime, ChildCareEvent> {

        return routines
            .filter { it.todayRoutine == ChildTodayRoutine.NURSERY }
            .groupBy { it.nurseryStart }
            .mapValues { (time, group) ->

                val childIds = group.map { it.childId }
                val eventId = "DROP_${date}_${time}"
                val duration = TimeAxis.stepMinutes // 拡張要素：dropOffSteps * TimeAxis.stepMinutes
                val earliest = group.maxOf { it.nurseryStartEarliest }
                val latest = group.minOf { it.nurseryStartLatest }

                ChildCareEvent(
                    eventId = eventId,
                    time = time,
                    duration = duration,
                    label = ChildCareLabel.NURSERY_DROP_OFF,
                    flexEarliest = earliest,
                    flexLatest = latest,
                    childIds = childIds
                )
            }
    }

    private fun buildPickupEvents(
        routines: List<ResolvedChildRoutine>,
        date: LocalDate
    ): Map<LocalTime, ChildCareEvent> {

        return routines
            .filter { it.todayRoutine == ChildTodayRoutine.NURSERY }
            .groupBy { it.nurseryEnd }
            .mapValues { (time, group) ->

                val childIds = group.map { it.childId }
                val eventId = "PICKUP_${date}_${time}"
                val duration = TimeAxis.stepMinutes // 拡張要素：pickupSteps * TimeAxis.stepMinutes
                val earliest = group.maxOf { it.nurseryEndEarliest }
                val latest = group.minOf { it.nurseryEndLatest }

                ChildCareEvent(
                    eventId = eventId,
                    time = time,
                    duration = duration,
                    label = ChildCareLabel.NURSERY_PICKUP,
                    flexEarliest = earliest,
                    flexLatest = latest,
                    childIds = childIds
                )
            }
    }

    private fun List<ChildCareEvent>.toBlocks(
        date: LocalDate
    ): List<ChildCareBlock> {

        val blocks = mutableListOf<ChildCareBlock>()

        forEach { event ->
            blocks += ChildCareBlock(
                eventId = event.eventId,
                daysOfWeek = setOf(date.dayOfWeek),
                startTime = event.time,
                endTime = event.time.plusMinutes(event.duration.toLong()),
                label = event.label,
                flexEarliest = event.flexEarliest,
                flexLatest = event.flexLatest,
                activeChildrenCount = event.childIds.size
            )
        }

        return blocks
    }
}