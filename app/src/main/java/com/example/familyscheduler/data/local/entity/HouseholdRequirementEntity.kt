package com.example.familyscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household_requirements")
data class HouseholdRequirementEntity(

    @PrimaryKey val id: String,

    val source: String,

    val taskName: String,
    val targetState: String,
    val requiredCount: Int,

    val allowedPersons: String, // CSV

    val flexBackward: Int,
    val flexForward: Int,

    val date: String?,          // LocalDate → String
    val daysOfWeek: String?,    // CSV

    val startTime: String,
    val endTime: String,

    val createdAt: Long
)