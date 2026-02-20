package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import kotlin.math.abs

data class FlexResolveProposal(
    val requirementName: String,
    val person: Person,
    val candidateIndex: Int,
    val initialIndex: Int,
    val deltaMinutes: Int,
    val targetState: SlotState
) {
    fun score(slots: List<TimeSlot>): Int {
        val candidateSlot = slots.find {
            it.person == person && it.index == candidateIndex
        } ?: return Int.MAX_VALUE

        val moveCost = candidateSlot.state.weight
        val distanceCost = abs(deltaMinutes) / 30

        return moveCost * 10 + distanceCost
    }
}

