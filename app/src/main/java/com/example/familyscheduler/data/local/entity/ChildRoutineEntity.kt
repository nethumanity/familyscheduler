package com.example.familyscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_routines")
data class ChildRoutineEntity(

    @PrimaryKey val childId: String,

    val childName: String,

    val wakeUpTime: String,
    val sleepTime: String,

    val daysOfWeek: String,

    val nurseryStart: String,
    val nurseryStartEarliest: String,
    val nurseryStartLatest: String,

    val nurseryEnd: String,
    val nurseryEndEarliest: String,
    val nurseryEndLatest: String
)