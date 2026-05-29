package com.example.familyscheduler.domain.interaction

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.requirement.RequirementSemantics

data class TimelineBlock(
    val startIndex: Int,
    val endIndex: Int,
    val semantics: RequirementSemantics,
    val mode: RequirementModeToday,
    val assignedPersons: List<Person>,
    val assignablePersons: List<Person>,
    val requiredCount: Int,
    val requirementIds: List<String>,
    val allowedActions: Set<BlockAction>
)
