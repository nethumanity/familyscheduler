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

    val allowedPersons: String,

    val flexBackward: Int,
    val flexForward: Int,

    val date: String?,
    val daysOfWeek: String?,

    val startTime: String,
    val endTime: String,

    val createdAt: Long
)