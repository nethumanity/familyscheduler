package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeAxis.stepMinutes
import java.time.DayOfWeek
import java.time.LocalTime

class ChildRoutineBuilder {

    fun build(routines: List<ResolvedChildRoutine>
    ): List<ChildCareBlock> {

        val blocks = mutableListOf<ChildCareBlock>()

        DayOfWeek.entries.forEach { day ->
            blocks += buildDayBlocks(day, routines)
        }

        return mergeSameBlocks(blocks)
    }

    private fun buildDayBlocks(
        day: DayOfWeek,
        routines: List<ResolvedChildRoutine>
    ): List<ChildCareBlock> {

        val baseUnits = buildBaseUnits(routines)

        val dropEvents = buildDropEvents(routines)
        val pickupEvents = buildPickupEvents(routines)

        val injected = injectEvents(baseUnits, dropEvents, pickupEvents)

        return compressUnits(injected, day)
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

            val end = start.plusMinutes(stepMinutes.toLong())

            val activeChildren = routines.count { child ->

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

    private data class EventInfo(
        val label: ChildCareLabel,
        val flexEarliest: LocalTime,
        val flexLatest: LocalTime
    )

    private fun buildDropEvents(
        routines: List<ResolvedChildRoutine>
    ): Map<LocalTime, EventInfo> {

        return routines
            .filter { it.todayRoutine == ChildTodayRoutine.NURSERY }
            .groupBy { it.nurseryStart }
            .mapValues { (_, group) ->

                val earliest = group.maxOf { it.nurseryStartEarliest }
                val latest = group.minOf { it.nurseryStartLatest }

                EventInfo(
                    label = ChildCareLabel.NURSERY_DROP_OFF,
                    flexEarliest = earliest,
                    flexLatest = latest
                )
            }
    }

    private fun buildPickupEvents(
        routines: List<ResolvedChildRoutine>
    ): Map<LocalTime, EventInfo> {

        return routines
            .filter { it.todayRoutine == ChildTodayRoutine.NURSERY }
            .groupBy { it.nurseryEnd }
            .mapValues { (_, group) ->

                val earliest = group.maxOf { it.nurseryEndEarliest }
                val latest = group.minOf { it.nurseryEndLatest }

                EventInfo(
                    label = ChildCareLabel.NURSERY_PICKUP,
                    flexEarliest = earliest,
                    flexLatest = latest
                )
            }
    }

    private fun injectEvents(
        baseUnits: List<BaseUnit>,
        dropEvents: Map<LocalTime, EventInfo>,
        pickupEvents: Map<LocalTime, EventInfo>
    ): List<ChildCareBlock> {

        return baseUnits.map { unit ->

            val event =
                dropEvents[unit.start]
                    ?: pickupEvents[unit.start]

            ChildCareBlock(
                daysOfWeek = emptySet(),
                startTime = unit.start,
                endTime = unit.end,
                label = event?.label,
                flexEarliest = event?.flexEarliest,
                flexLatest = event?.flexLatest,
                activeChildrenCount = unit.activeChildrenCount
            )

            /*
            if (event != null) {
                ChildCareBlock(
                    daysOfWeek = emptySet(), // ← compress時に設定
                    startTime = unit.start,
                    endTime = unit.end,
                    label = event.label,
                    flexEarliest = event.flexEarliest,
                    flexLatest = event.flexLatest,
                    activeChildrenCount = unit.activeChildrenCount
                )
            } else {
                ChildCareBlock(
                    daysOfWeek = emptySet(),
                    startTime = unit.start,
                    endTime = unit.end,
                    label = null,
                    flexEarliest = null,
                    flexLatest = null,
                    activeChildrenCount = unit.activeChildrenCount
                )
            }
             */
        }
    }

    private fun compressUnits(
        blocks: List<ChildCareBlock>,
        day: DayOfWeek
    ): List<ChildCareBlock> {

        if (blocks.isEmpty()) return emptyList()

        val result = mutableListOf<ChildCareBlock>()
        var current = blocks.first()

        for (next in blocks.drop(1)) {

            val mergeable =
                current.label == next.label &&
                        current.activeChildrenCount == next.activeChildrenCount &&
                        current.flexEarliest == next.flexEarliest &&
                        current.flexLatest == next.flexLatest

            if (mergeable) {
                current = current.copy(
                    endTime = next.endTime
                )
            } else {
                result += current.copy(daysOfWeek = setOf(day))
                current = next
            }
        }

        result += current.copy(daysOfWeek = setOf(day))

        return result
    }

    data class BlockKey(
        val start: LocalTime,
        val end: LocalTime,
        val label: ChildCareLabel?,
        val count: Int,
        val earliest: LocalTime?,
        val latest: LocalTime?
    )

    private fun mergeSameBlocks(
        blocks: List<ChildCareBlock>
    ): List<ChildCareBlock> {

        return blocks
            .groupBy {
                BlockKey(
                    start = it.startTime,
                    end = it.endTime,
                    label = it.label,
                    count = it.activeChildrenCount,
                    earliest = it.flexEarliest,
                    latest = it.flexLatest
                )
            }
            .map { (_, group) ->

                group.first().copy(
                    daysOfWeek = group.flatMap { it.daysOfWeek }
                        .toSet()
                )
            }
    }
}