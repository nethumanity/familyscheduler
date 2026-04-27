package com.example.familyscheduler.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "routine_shift_overrides",
    primaryKeys = ["childId", "date", "eventType"]
)
data class RoutineShiftOverrideEntity(

    val childId: String,

    val date: String,

    val eventType: String,

    val nurseryTime: String
)