package com.example.familyscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_overrides")
data class ChildOverrideEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val childName: String,
    val date: String,      // LocalDate → String
    val routine: String    // enum → String
)