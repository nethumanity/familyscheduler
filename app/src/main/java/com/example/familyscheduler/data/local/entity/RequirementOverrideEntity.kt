package com.example.familyscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requirement_overrides")
data class RequirementOverrideEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val ruleId: String,
    val date: String,       // LocalDate → String

    val type: String,       // "TOGGLE" or "SHIFT"

    val mode: String?,      // TOGGLE用
    val deltaSteps: Int?    // SHIFT用
)