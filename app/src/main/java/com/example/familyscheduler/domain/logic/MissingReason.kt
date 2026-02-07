package com.example.familyscheduler.domain.logic

import com.example.familyscheduler.domain.model.Person
import com.example.familyscheduler.domain.model.RequirementType
import com.example.familyscheduler.domain.model.SlotState

sealed class MissingReason {
    data class NotEnoughPeople(
        val requirementName: String,
        val required: Int,
        val assigned: Int
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