package com.example.familyscheduler.domain.routine.repository

import com.example.familyscheduler.domain.routine.ChildTodayRoutine
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

    fun getAll(): Map<Pair<String, LocalDate>, ChildTodayRoutine>

    suspend fun deleteByChildName(childName: String)
}