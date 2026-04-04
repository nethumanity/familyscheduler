package com.example.familyscheduler.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "daily_states",
    primaryKeys = ["date", "person"]
)
data class DailyStateEntity(
    val date: String,           // LocalDate → String
    val person: String,
    val templateName: String,
    val slotsJson: String       // List<TimeSlot> をJSON化
)