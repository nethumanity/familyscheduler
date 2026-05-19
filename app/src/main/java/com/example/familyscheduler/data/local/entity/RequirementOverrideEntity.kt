package com.example.familyscheduler.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "requirement_overrides",
    primaryKeys = ["ruleId", "date", "type"]
)
data class RequirementOverrideEntity(

    val ruleId: String,
    val date: String,

    val type: String,

    val mode: String?,
    val deltaSteps: Int?
)