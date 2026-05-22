package com.example.familyscheduler.domain.interaction

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementSemantics

data class ReverseAssignableBlock(
    val startIndex: Int,
    val endIndex: Int,
    val semantics: RequirementSemantics,
    val assignedPerson: Person,
    val reversedPerson: Person,
    val requirementIds: List<String>
)