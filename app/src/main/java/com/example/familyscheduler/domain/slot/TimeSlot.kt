package com.example.familyscheduler.domain.slot

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementSemantics

data class TimeSlot(
    val index: Int,
    val person: Person,
    val state: SlotState,
    val taskIds: List<String>,
    val effectiveSemantics: RequirementSemantics = RequirementSemantics.STATE
)