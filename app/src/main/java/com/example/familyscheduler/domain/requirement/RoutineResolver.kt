package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.requirement.repository.ChildOverrideRepository
import java.time.DayOfWeek
import java.time.LocalDate

class RoutineResolver(
    private val overrideRepository: ChildOverrideRepository
) {

    fun resolve(
        inputs: List<ChildRoutineInput>,
        date: LocalDate
    ): List<ResolvedChildRoutine> {
        return inputs.map { input ->
            resolveSingle(input, date)
        }
    }

    private fun resolveSingle(
        input: ChildRoutineInput,
        date: LocalDate
    ): ResolvedChildRoutine {

        val override =
            overrideRepository.getOverride(input.name, date)

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