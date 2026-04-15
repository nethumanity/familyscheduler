package com.example.familyscheduler.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "routine_shift_overrides",
    primaryKeys = ["childId", "date", "eventType"]
)
data class RoutineShiftOverrideEntity(

    val childId: String,

    val date: String, // LocalDate → String

    val eventType: String, // enum → String（DROP_OFF / PICKUP）

    val nurseryTime: String  // LocalTime → String
)