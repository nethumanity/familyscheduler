package com.example.familyscheduler.domain.routine

class CareCapacityCalculator {

    fun calculateRequiredCount(
        activeChildrenCount: Int,
        maxChildrenPerAdult: Int
    ): Int {
        if (activeChildrenCount <= 0) return 0

        return (activeChildrenCount + maxChildrenPerAdult - 1) / maxChildrenPerAdult
    }
}