package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import kotlin.math.abs

data class FlexResolveProposal(
    val type: ProposalType,
    val resolvedRequirementId: String,
    val sourceRuleId: String,
    val requirementSource: RequirementSource,
    val requirementName: String,
    val persons: List<Person>,
    val requiredCount: Int,
    val initialIndex: Int,
    val candidateIndex: Int,
    val targetState: SlotState
) {
    fun score(slots: List<TimeSlot>): Int {

        val scores =
            persons.mapNotNull { person ->

                val candidateSlot = slots.find {
                    it.person == person &&
                            it.index == candidateIndex
                } ?: return@mapNotNull null

                val moveCost =
                    if (candidateSlot.state == targetState) 0
                    else candidateSlot.state.weight

                val distanceCost =
                    abs(candidateIndex - initialIndex)

                moveCost * 10 + distanceCost
            }

        if (scores.isEmpty()) {
            return Int.MAX_VALUE
        }

        return when {
            persons.size >= 2 && requiredCount >= 2 -> scores.max()
            persons.size >= 2 -> scores.min()
            else -> scores.first()
        }
    }
}