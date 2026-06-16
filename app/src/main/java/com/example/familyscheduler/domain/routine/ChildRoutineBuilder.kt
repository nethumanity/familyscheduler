package com.example.familyscheduler.domain.routine

import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.state.SettingsUiState
import java.time.LocalDate
import java.time.LocalTime

class ChildRoutineBuilder {

    fun build(
        date: LocalDate,
        routines: List<ResolvedChildRoutine>,
        settings: SettingsUiState
    ): RoutineResult {

        val blocks = buildBlocks(routines, date, settings)

        val dropEvents = buildEvents(
            routines = routines,
            date = date,
            targetFilter = { it.todayRoutine == ChildTodayRoutine.NURSERY },
            timeSelector = { it.nurseryStart },
            earliestSelector = { it.nurseryStartEarliest },
            latestSelector = { it.nurseryStartLatest },
            durationSteps = settings.dropOffSteps,
            label = ChildCareLabel.NURSERY_DROP_OFF,
            idPrefix = "DROP"
        )

        val pickupEvents = buildEvents(
            routines = routines,
            date = date,
            targetFilter = { it.todayRoutine == ChildTodayRoutine.NURSERY },
            timeSelector = { it.nurseryEnd },
            earliestSelector = { it.nurseryEndEarliest },
            latestSelector = { it.nurseryEndLatest },
            durationSteps = settings.pickupSteps,
            label = ChildCareLabel.NURSERY_PICKUP,
            idPrefix = "PICKUP"
        )

        val bedtimeEvents = buildEvents(
            routines = routines,
            date = date,
            targetFilter = { it.todayRoutine != ChildTodayRoutine.NONE },
            timeSelector = { it.sleepTime },
            earliestSelector = { it.sleepTime },
            latestSelector = { it.sleepTime },
            durationSteps = settings.bedtimeSteps,
            label = ChildCareLabel.BEDTIME,
            idPrefix = "BEDTIME"
        )

        val events = dropEvents + pickupEvents + bedtimeEvents

        return RoutineResult(
            blocks = blocks + events.toBlocks(date),
            events = events
        )
    }

    private fun buildBlocks(
        routines: List<ResolvedChildRoutine>,
        date: LocalDate,
        settings: SettingsUiState
    ): List<ChildCareBlock> {

        return TimeAxis.all.mapNotNull { start ->

            val end = start.plusMinutes(TimeAxis.stepMinutes.toLong())

            val activeChildren = routines.count { child ->

                if (child.todayRoutine == ChildTodayRoutine.NONE) {
                    return@count false
                }

                start >= child.wakeUpTime &&
                        start < child.sleepTime &&
                        (
                                child.todayRoutine == ChildTodayRoutine.HOME ||
                                        start < child.nurseryStart ||
                                        start >= child.nurseryEnd.plusMinutes(
                                    (settings.pickupSteps * TimeAxis.stepMinutes).toLong()
                                        )
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
        targetFilter: (ResolvedChildRoutine) -> Boolean = { true },
        timeSelector: (ResolvedChildRoutine) -> LocalTime,
        earliestSelector: (ResolvedChildRoutine) -> LocalTime,
        latestSelector: (ResolvedChildRoutine) -> LocalTime,
        durationSteps: Int,
        label: ChildCareLabel,
        idPrefix: String
    ): List<ChildCareEvent> {

        if (durationSteps == 0) return emptyList()

        return routines
            .filter(targetFilter)
            .groupBy(timeSelector)
            .map { (time, group) ->

                ChildCareEvent(
                    eventId = "${idPrefix}_${date}_$time",
                    time = time,
                    duration = durationSteps * TimeAxis.stepMinutes,
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