package com.example.familyscheduler.domain.evaluation

data class MissingReason(
    val sourceRuleId: String,
    val requirementName: String,
    val requiredCount: Int,
    val assignedCount: Int,
    val blockingPersons: BlockInfo
)
