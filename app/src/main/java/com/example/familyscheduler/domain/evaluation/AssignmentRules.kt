package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot

object AssignmentRules {

    private val overridableStates = setOf(
        SlotState.UNASSIGNED,
        SlotState.FREE,
        SlotState.REST
    )

    fun canAssign(
        req: HouseholdRequirement,
        slot: TimeSlot
    ): Boolean {

        if (
            slot.state != req.targetState &&
            slot.state !in overridableStates
        ) {
            return false
        }

        return req.source.semantics.canCoexist(
            slot.effectiveSemantics
        )
    }

    fun canReverseAssign(
        req: HouseholdRequirement,
        slot: TimeSlot,
        priorityResolver: (String) -> Long?
    ): Boolean {

        if (canAssign(req, slot)) {
            return true
        }

        if (slot.taskIds.isEmpty()) {
            return false
        }

        val blockingPriority =
            slot.taskIds.maxOf { id ->
                priorityResolver(id) ?: Long.MAX_VALUE
            }

        return req.prioritySeed > blockingPriority
    }
}