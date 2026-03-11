package com.example.familyscheduler.domain.requirement.repository

import com.example.familyscheduler.domain.requirement.ChildTodayRoutine
import java.time.LocalDate

interface ChildOverrideRepository {

    fun getOverride(
        childName: String,
        date: LocalDate
    ): ChildTodayRoutine?

    fun saveOverride(
        childName: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    )
}