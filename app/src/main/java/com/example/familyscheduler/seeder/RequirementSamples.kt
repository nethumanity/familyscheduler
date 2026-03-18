package com.example.familyscheduler.seeder

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

object RequirementSamples {

    fun defaultRequirements(): List<HouseholdRequirementRule> = listOf(

        HouseholdRequirementRule(
            id = UUID.randomUUID().toString(),
            source = RequirementSource.USER,
            taskName = "朝食",
            targetState = SlotState.LIFE,
            requiredCount = 1,
            allowedPersons = setOf(Person.FATHER),
            flexWindowSlots = FlexWindowParameters(0, 0),
            date = null,
            daysOfWeek = DayOfWeek.values().toSet(),
            timeRange = TimeRange(
                start = LocalTime.of(7, 0),
                end = LocalTime.of(7, 30)
            )
        ),

        HouseholdRequirementRule(
            id = UUID.randomUUID().toString(),
            source = RequirementSource.USER,
            taskName = "朝食",
            targetState = SlotState.CHILDCARE,
            requiredCount = 1,
            allowedPersons = setOf(Person.MOTHER),
            flexWindowSlots = FlexWindowParameters(0,0),
            date = null,
            daysOfWeek = DayOfWeek.values().toSet(),
            timeRange = TimeRange(
                start = LocalTime.of(7,0),
                end = LocalTime.of(7,30)
            )
        ),

        HouseholdRequirementRule(
            id = UUID.randomUUID().toString(),
            source = RequirementSource.USER,
            taskName = "夕食準備",
            targetState = SlotState.LIFE,
            requiredCount = 1,
            allowedPersons = setOf(
                Person.FATHER,
                Person.MOTHER
            ),
            flexWindowSlots = FlexWindowParameters(1,1),
            date = null,
            daysOfWeek = DayOfWeek.values().toSet(),
            timeRange = TimeRange(
                LocalTime.of(18,0),
                LocalTime.of(18,30)
            )
        ),

        HouseholdRequirementRule(
            id = UUID.randomUUID().toString(),
            source = RequirementSource.USER,
            taskName = "夕食",
            targetState = SlotState.CHILDCARE,
            requiredCount = 2,
            allowedPersons = setOf(
                Person.FATHER,
                Person.MOTHER
            ),
            flexWindowSlots = FlexWindowParameters(1,1),
            date = null,
            daysOfWeek = DayOfWeek.values().toSet(),
            timeRange = TimeRange(
                LocalTime.of(18,30),
                LocalTime.of(19,0)
            )
        ),

        HouseholdRequirementRule(
            id = UUID.randomUUID().toString(),
            source = RequirementSource.USER,
            taskName = "買い物",
            targetState = SlotState.LIFE,
            requiredCount = 1,
            allowedPersons = setOf(
                Person.FATHER,
                Person.MOTHER
            ),
            flexWindowSlots = FlexWindowParameters(3,3),
            date = null,
            daysOfWeek = setOf(
                DayOfWeek.TUESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.SATURDAY
            ),
            timeRange = TimeRange(
                LocalTime.of(16,0),
                LocalTime.of(17,0)
            )
        ),

        HouseholdRequirementRule(
            id = UUID.randomUUID().toString(),
            source = RequirementSource.USER,
            taskName = "通院",
            targetState = SlotState.LIFE,
            requiredCount = 1,
            allowedPersons = setOf(Person.MOTHER),
            flexWindowSlots = FlexWindowParameters(0,0),
            date = LocalDate.now(),
            daysOfWeek = null,
            timeRange = TimeRange(
                LocalTime.of(10,0),
                LocalTime.of(11,0)
            )
        )
    )
}