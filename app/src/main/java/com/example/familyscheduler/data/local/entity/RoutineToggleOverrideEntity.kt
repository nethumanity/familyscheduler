package com.example.familyscheduler.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "child_overrides",
    primaryKeys = ["childId", "date"]
)
data class RoutineToggleOverrideEntity(

    val childId: String,

    val date: String,      // LocalDate → String

    val routine: String    // enum → String
)