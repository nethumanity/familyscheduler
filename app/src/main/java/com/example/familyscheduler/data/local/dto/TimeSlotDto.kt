package com.example.familyscheduler.data.local.dto

data class TimeSlotDto(
    val index: Int,
    val person: String,
    val state: String,
    val backward: Int,
    val forward: Int,
    val taskNames: List<String>
)