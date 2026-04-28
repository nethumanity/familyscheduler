package com.example.familyscheduler.domain.routine

import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalDate
import java.time.LocalTime

class ChildRoutineBuilder {

    fun build(
        date: LocalDate,
        routines: List<ResolvedChildRoutine>
    ): RoutineResult {

        val baseUnits = buildBaseUnits(routines)

        val dropEvents = buildDropEvents(routines, date)
        val pickupEvents = buildPickupEvents(routines, date)
        val events = dropEvents.values + pickupEvents.values

        val injected = injectEvents(baseUnits, dropEvents, pickupEvents, date)

        return RoutineResult(compressUnits(injected), events)
    }

    private data class BaseUnit(
        val start: LocalTime,
        val end: LocalTime,
        val activeChildrenCount: Int
    )

    private fun buildBaseUnits(
        routines: List<ResolvedChildRoutine>
    ): List<BaseUnit> {

        val units = mutableListOf<BaseUnit>()

        for (start in TimeAxis.all) {

            val end = start.plusMinutes(TimeAxis.stepMinutes.toLong())

            val activeChildren = routines.count { child ->

                if (child.todayRoutine == ChildTodayRoutine.NONE) {
                    return@count false
                }

                val needsCare =
                    start >= child.wakeUpTime &&
                            start <= child.sleepTime &&
                            (
                                    child.todayRoutine == ChildTodayRoutine.HOME ||
                                            start <= child.nurseryStart ||
                                            start >= child.nurseryEnd
                                    )

                needsCare
            }

            if (activeChildren > 0) {
                units += BaseUnit(
                    start = start,
                    end = end,
                    activeChildrenCount = activeChildren
                )
            }
        }

        return units
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

    private fun injectEvents(
        baseUnits: List<BaseUnit>,
        dropEvents: Map<LocalTime, ChildCareEvent>,
        pickupEvents: Map<LocalTime, ChildCareEvent>,
        date: LocalDate
    ): List<ChildCareBlock> {

        return baseUnits.map { unit ->

            val event =
                dropEvents[unit.start]
                    ?: pickupEvents[unit.start]

            ChildCareBlock(
                eventId = event?.eventId ?: "CHILDCARE_${date}_${unit.start}",
                daysOfWeek = setOf(date.dayOfWeek),
                startTime = unit.start,
                endTime = unit.end,
                label = event?.label,
                flexEarliest = event?.flexEarliest,
                flexLatest = event?.flexLatest,
                activeChildrenCount = unit.activeChildrenCount
            )
        }
    }

    private fun compressUnits(
        blocks: List<ChildCareBlock>
    ): List<ChildCareBlock> {

        if (blocks.isEmpty()) return emptyList()

        val result = mutableListOf<ChildCareBlock>()
        var current = blocks.first()

        for (next in blocks.drop(1)) {

            val mergeable =
                current.eventId == next.eventId &&
                        current.label == next.label &&
                        current.activeChildrenCount == next.activeChildrenCount &&
                        current.flexEarliest == next.flexEarliest &&
                        current.flexLatest == next.flexLatest

            if (mergeable) {
                current = current.copy(
                    endTime = next.endTime
                )
            } else {
                result += current
                current = next
            }
        }
        result += current

        return result
    }
}