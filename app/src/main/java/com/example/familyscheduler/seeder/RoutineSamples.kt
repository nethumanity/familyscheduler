package com.example.familyscheduler.seeder

import com.example.familyscheduler.domain.routine.ChildRoutineInput
import java.time.DayOfWeek
import java.time.LocalTime

object RoutineSamples {

    fun defaultRoutine(): List<ChildRoutineInput> = listOf(

        ChildRoutineInput(
            name = "NALE",
            wakeUpTime = LocalTime.of(6,30),
            sleepTime = LocalTime.of(20,0),
            daysOfWeek = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ),
            nurseryStart = LocalTime.of(8,30),
            nurseryStartEarliest = LocalTime.of(7,0),
            nurseryStartLatest = LocalTime.of(9,0),
            nurseryEnd = LocalTime.of(17,30),
            nurseryEndEarliest = LocalTime.of(17,0),
            nurseryEndLatest = LocalTime.of(20,0)
        ),

        ChildRoutineInput(
            name = "JOE",
            wakeUpTime = LocalTime.of(6,30),
            sleepTime = LocalTime.of(20,0),
            daysOfWeek = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ),
            nurseryStart = LocalTime.of(8,0),
            nurseryStartEarliest = LocalTime.of(7,0),
            nurseryStartLatest = LocalTime.of(9,0),
            nurseryEnd = LocalTime.of(18,0),
            nurseryEndEarliest = LocalTime.of(18,0),
            nurseryEndLatest = LocalTime.of(20,0)
        )
    )
}