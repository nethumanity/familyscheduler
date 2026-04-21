package com.example.familyscheduler.domain.routine

class CareCapacityCalculator(
    //private val maxChildrenPerAdult: Int = 2
) {

    fun calculateRequiredCount(
        activeChildrenCount: Int,
        maxChildrenPerAdult: Int
    ): Int {
        if (activeChildrenCount <= 0) return 0

        return (activeChildrenCount + maxChildrenPerAdult - 1) / maxChildrenPerAdult
    }
}