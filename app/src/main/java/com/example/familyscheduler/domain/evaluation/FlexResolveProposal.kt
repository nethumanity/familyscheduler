package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import kotlin.math.abs

data class FlexResolveProposal(
    val type: ProposalType,
    val sourceRuleId: String,
    val requirementSource: RequirementSource,
    val requirementName: String,
    val persons: List<Person>,
    val initialIndex: Int,
    val candidateIndex: Int,
    val targetState: SlotState
) {
    fun score(slots: List<TimeSlot>): Int {
        val candidateSlot = slots.find {
            it.person in persons && it.index == candidateIndex
        } ?: return Int.MAX_VALUE

        val moveCost =
            if (candidateSlot.state == targetState) 0
            else candidateSlot.state.weight

        val distanceCost = abs(candidateIndex - initialIndex)

        return moveCost * 10 + distanceCost
    }
}