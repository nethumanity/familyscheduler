package com.example.familyscheduler.domain.routine

import java.time.DayOfWeek
import java.time.LocalDate

class RoutineResolver {

    fun resolve(
        inputs: List<ChildRoutineInput>,
        date: LocalDate,
        childOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>
    ): List<ResolvedChildRoutine> {
        return inputs.map { input ->
            resolveSingle(input, date, childOverrides)
        }
    }

    private fun resolveSingle(
        input: ChildRoutineInput,
        date: LocalDate,
        childOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>
    ): ResolvedChildRoutine {

        val override = childOverrides[input.name to date]

        val todayRoutine =
            override ?: defaultRoutine(input, date.dayOfWeek)

        return ResolvedChildRoutine(
            name = input.name,
            wakeUpTime = input.wakeUpTime,
            sleepTime = input.sleepTime,
            todayRoutine = todayRoutine,
            nurseryStart = input.nurseryStart,
            nurseryEnd = input.nurseryEnd,
            nurseryStartEarliest = input.nurseryStartEarliest,
            nurseryStartLatest = input.nurseryStartLatest,
            nurseryEndEarliest = input.nurseryEndEarliest,
            nurseryEndLatest = input.nurseryEndLatest
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
}