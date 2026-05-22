package com.example.familyscheduler.domain.interaction

import com.example.familyscheduler.domain.evaluation.AvailabilityEngine
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.RequirementSemantics
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.slot.TimeSlot

class ReverseAssignableBlockBuilder {

    fun build(
        requirements: List<HouseholdRequirement>,
        slotsByIndex: Map<Int, List<TimeSlot>>,
        slotsByPersonIndex: Map<Pair<Person, Int>, TimeSlot>
    ): List<ReverseAssignableBlock> {

        val items = requirements
            .filterIsInstance<TimeRangeHouseholdRequirement>()
            .filter { it.source == RequirementSource.CHILD_ROUTINE }
            .mapNotNull { req ->
                if (req.requiredCount != 1) return@mapNotNull null

                val startIndex = req.startIndex

                val assignedPersons =
                    findAssignedPersons(
                        req = req,
                        slotsByIndex = slotsByIndex
                    )

                if (assignedPersons.size != 1) return@mapNotNull null

                val assignedPerson = assignedPersons.single()

                val reversedCandidates =
                    req.allowedPersons.filter {
                        it != assignedPerson
                    }

                if (reversedCandidates.size != 1) return@mapNotNull null

                val reversedPerson = reversedCandidates.single()

                val reverseTargetSlot = slotsByPersonIndex[reversedPerson to startIndex]
                    ?: return@mapNotNull null

                val reversible =
                    AvailabilityEngine.canAssignToSlot(
                        req = req,
                        slot = reverseTargetSlot
                    )

                if (!reversible) return@mapNotNull null

                ReverseAssignableBlock(
                    startIndex = startIndex,
                    endIndex = startIndex + 1,
                    semantics = RequirementSemantics.STATE,
                    assignedPerson = assignedPerson,
                    reversedPerson = reversedPerson,
                    requirementIds = listOf(req.sourceRuleId)
                )
            }

        if (items.isEmpty()) return emptyList()

        val sorted = items.sortedBy { it.startIndex }

        val merged = mutableListOf<MutableList<ReverseAssignableBlock>>()

        var current = mutableListOf(sorted.first())


        for (item in sorted.drop(1)) {

            val prev = current.last()

            val mergeable =
                prev.endIndex == item.startIndex &&
                        prev.assignedPerson == item.assignedPerson &&
                        prev.reversedPerson == item.reversedPerson &&
                        prev.semantics == item.semantics

            if (mergeable) {
                current += item
            } else {
                merged += current
                current = mutableListOf(item)
            }

        }

        merged += current

        return merged.map { group ->

            ReverseAssignableBlock(
                startIndex = group.first().startIndex,
                endIndex = group.last().endIndex,
                semantics = group.first().semantics,
                assignedPerson = group.first().assignedPerson,
                reversedPerson = group.first().reversedPerson,
                requirementIds = group.flatMap { it.requirementIds }
            )
        }
    }

    fun findAssignedPersons(
        req: TimeRangeHouseholdRequirement,
        slotsByIndex: Map<Int, List<TimeSlot>>
    ): List<Person> {
        return slotsByIndex[req.startIndex]
                .orEmpty()
                .filter {
                    it.state == req.targetState &&
                            it.person in req.allowedPersons &&
                            req.sourceRuleId in it.taskIds
                }
                .map { it.person }
    }
}