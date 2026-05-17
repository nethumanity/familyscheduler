package com.example.familyscheduler.data.local.dto

import com.example.familyscheduler.domain.requirement.RequirementSemantics

data class TimeSlotDto(
    val index: Int,
    val person: String,
    val state: String,
    val taskIds: List<String>,
    val effectiveSemantics: String = RequirementSemantics.STATE.name
)