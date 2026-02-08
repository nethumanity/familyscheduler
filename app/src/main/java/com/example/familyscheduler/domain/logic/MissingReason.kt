package com.example.familyscheduler.domain.logic

import com.example.familyscheduler.domain.model.Person
import com.example.familyscheduler.domain.model.RequirementType
import com.example.familyscheduler.domain.model.SlotState

sealed class MissingReason {
    data class NotEnoughPeople(
        val requirementName: String,
        val requiredCount: Int,
        val assignedCount: Int,
        val blockingPersons: List<BlockInfo>
    ) : MissingReason()

    data class NoAssignablePerson(
        val requirementName: String
    ) : MissingReason()

    data class StateConflict(
        val person: Person,
        val expected: SlotState,
        val actual: SlotState
    ) : MissingReason()
}

data class BlockInfo(
    val person: List<Person>,
    val currentState: List<SlotState>,
    val taskName: String?
)
