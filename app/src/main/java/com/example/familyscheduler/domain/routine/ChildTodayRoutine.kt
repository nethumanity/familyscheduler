package com.example.familyscheduler.domain.routine

enum class ChildTodayRoutine {
    NURSERY,     // 登園
    HOME,        // 在宅
    NONE;        // 保育なし

    fun next(): ChildTodayRoutine {
        return when (this) {
            NURSERY -> HOME
            HOME -> NONE
            NONE -> NURSERY
        }
    }
}

