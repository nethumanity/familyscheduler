package com.example.familyscheduler.domain.routine

enum class ChildTodayRoutine {
    NURSERY,     // 登園
    HOME,        // 在宅
    NONE;        // 保育なし

    fun next(input: ChildRoutineInput): ChildTodayRoutine {
        return if (input.daysOfWeek.isNotEmpty()) {
            when (this) {
                NURSERY -> HOME
                HOME -> NONE
                NONE -> NURSERY
            }
        } else {
            when (this) {
                HOME -> NONE
                NONE -> HOME
                NURSERY -> HOME
            }
        }
    }
}

