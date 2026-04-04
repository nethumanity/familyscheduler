package com.example.familyscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val person: String,
    val name: String,
    val schedulesJson: String,
    val repeatType: String,
    val repeatDays: String?,
    val createdAt: Long
)