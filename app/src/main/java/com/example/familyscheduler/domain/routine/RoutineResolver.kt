package com.example.familyscheduler.domain.routine

import java.time.DayOfWeek
import java.time.LocalDate

class RoutineResolver {

    fun resolve(
        routines: List<ChildRoutineInput>,
        date: LocalDate,
        toggleOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>,
        routineShiftByIdEvent: Map<Pair<String, ChildCareLabel>, RoutineShiftOverride>
    ): List<ResolvedChildRoutine> {

        val base = routines.map { routine ->
            resolveSingle(routine, date, toggleOverrides)
        }

        return applyRoutineOverrides(base, routineShiftByIdEvent)
    }

    private fun resolveSingle(
        routine: ChildRoutineInput,
        date: LocalDate,
        toggleOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>
    ): ResolvedChildRoutine {

        val override = toggleOverrides[routine.childId to date]

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
        routineShiftByIdEvent: Map<Pair<String, ChildCareLabel>, RoutineShiftOverride>
    ): List<ResolvedChildRoutine> {

        return routines.map { routine ->

            val startShift = routineShiftByIdEvent[routine.childId to ChildCareLabel.NURSERY_DROP_OFF]
            val endShift = routineShiftByIdEvent[routine.childId to ChildCareLabel.NURSERY_PICKUP]

            routine.copy(
                nurseryStart = startShift?.nurseryTime ?: routine.nurseryStart,
                nurseryEnd = endShift?.nurseryTime ?: routine.nurseryEnd
            )
        }
    }
}