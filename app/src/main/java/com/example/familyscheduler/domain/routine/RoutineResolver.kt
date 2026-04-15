package com.example.familyscheduler.domain.routine

import java.time.DayOfWeek
import java.time.LocalDate

class RoutineResolver {

    fun resolve(
        routines: List<ChildRoutineInput>,
        date: LocalDate,
        childOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>,
        routineShiftOverrides: List<RoutineShiftOverride>
    ): List<ResolvedChildRoutine> {

        val base = routines.map { routine ->
            resolveSingle(routine, date, childOverrides)
        }

        return applyRoutineOverrides(base, routineShiftOverrides)
    }

    private fun resolveSingle(
        routine: ChildRoutineInput,
        date: LocalDate,
        childOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>
    ): ResolvedChildRoutine {

        val override = childOverrides[routine.childId to date]

        val todayRoutine =
            override ?: defaultRoutine(routine, date.dayOfWeek)

        return ResolvedChildRoutine(
            childId = routine.childId,
            childName = routine.childName,
            wakeUpTime = routine.wakeUpTime,
            sleepTime = routine.sleepTime,
            todayRoutine = todayRoutine,
            nurseryStart = routine.nurseryStart,
            nurseryEnd = routine.nurseryEnd,
            nurseryStartEarliest = routine.nurseryStartEarliest,
            nurseryStartLatest = routine.nurseryStartLatest,
            nurseryEndEarliest = routine.nurseryEndEarliest,
            nurseryEndLatest = routine.nurseryEndLatest
        )
    }

    private fun defaultRoutine(
        input: ChildRoutineInput,
        day: DayOfWeek
    ): ChildTodayRoutine {

        return if (day in input.daysOfWeek) {
            ChildTodayRoutine.NURSERY
        } else {
            ChildTodayRoutine.HOME
        }
    }

    private fun applyRoutineOverrides(
        routines: List<ResolvedChildRoutine>,
        overrides: List<RoutineShiftOverride>
    ): List<ResolvedChildRoutine> {

        val shiftMap = overrides
            .associateBy { it.childId to it.eventType }

        return routines.map { routine ->

            val startShift = shiftMap[routine.childId to ChildCareLabel.NURSERY_DROP_OFF]
            val endShift = shiftMap[routine.childId to ChildCareLabel.NURSERY_PICKUP]

            routine.copy(
                nurseryStart = startShift?.nurseryTime ?: routine.nurseryStart,
                nurseryEnd = endShift?.nurseryTime ?: routine.nurseryEnd
            )
        }
    }
}