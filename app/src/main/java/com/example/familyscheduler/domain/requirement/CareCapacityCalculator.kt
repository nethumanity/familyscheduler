package com.example.familyscheduler.domain.requirement

class CareCapacityCalculator(
    private val maxChildrenPerAdult: Int = 2
) {

    fun calculateRequiredCount(activeChildrenCount: Int): Int {
        if (activeChildrenCount <= 0) return 0

        return (activeChildrenCount + maxChildrenPerAdult - 1) / maxChildrenPerAdult
    }
}