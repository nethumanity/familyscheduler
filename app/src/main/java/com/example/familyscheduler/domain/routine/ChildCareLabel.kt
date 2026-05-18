package com.example.familyscheduler.domain.routine

import com.example.familyscheduler.domain.requirement.RequirementSource

enum class ChildCareLabel(
    val taskName: String,
    val source: RequirementSource
) {
    NURSERY_DROP_OFF(
        taskName = "登園",
        source = RequirementSource.NURSERY_DROP_OFF
    ),
    NURSERY_PICKUP(
        taskName = "お迎え",
        source = RequirementSource.NURSERY_PICKUP
    )
}