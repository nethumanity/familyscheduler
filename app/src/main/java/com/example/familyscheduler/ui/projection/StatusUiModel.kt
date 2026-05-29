package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.interaction.TimelineBlock
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementModeToday

sealed interface StatusUiModel {

    data class Assigned(
        val persons: List<Person>,
        val requiredCount: Int
    ) : StatusUiModel

    data class Warning(
        val assignableCount: Int,
        val requiredCount: Int
    ) : StatusUiModel

    data class Reverse(
        val mainPerson: Person,
        val anotherPerson: Person
    ) : StatusUiModel

    data class Solo(
        val assignedPerson: Person,
        val blockedPerson: Person
    ) : StatusUiModel

    data object Canceled : StatusUiModel

    fun TimelineBlock.toStatusUiModel(): StatusUiModel {

        return when (mode) {

            RequirementModeToday.CANCELED -> {
                Canceled
            }

            RequirementModeToday.REVERSE -> {
                when (assignablePersons.size) {
                    0 -> Warning(
                        assignableCount = assignablePersons.size,
                        requiredCount = requiredCount
                    )
                    1 -> Assigned(
                        persons = assignedPersons,
                        requiredCount = requiredCount
                    )
                    else -> Reverse(
                        mainPerson = assignedPersons.first(),
                        anotherPerson = (Person.entries - assignedPersons).first()
                    )
                }
            }

            RequirementModeToday.SOLO -> {
                when (assignablePersons.size) {
                    0 -> Warning(
                        assignableCount = assignablePersons.size,
                        requiredCount = requiredCount
                    )
                    1 -> Solo(
                        assignedPerson = assignedPersons.first(),
                        blockedPerson = (Person.entries - assignedPersons).first()
                    )
                    else -> Assigned(
                        persons = assignedPersons,
                        requiredCount = requiredCount
                    )
                }
            }

            RequirementModeToday.AUTO -> {
                val isWarn = assignablePersons.size < requiredCount

                if (isWarn) {
                    Warning(
                        assignableCount = assignablePersons.size,
                        requiredCount = requiredCount
                    )
                } else {
                    Assigned(
                        persons = assignablePersons,
                        requiredCount = requiredCount
                    )
                }
            }
        }
    }
}