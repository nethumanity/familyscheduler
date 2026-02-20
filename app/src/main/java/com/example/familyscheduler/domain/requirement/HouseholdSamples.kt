package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalTime

object HouseholdSamples {

    fun default(): List<HouseholdRequirement> = listOf(

        // 朝食（3人で食べる → 親2人拘束）
        TimeRangeHouseholdRequirement(
            name = "朝食",
            targetState = SlotState.LIFE,
            requiredCount = 1,
            allowedPersons = setOf(Person.FATHER),
            type = RequirementType.FIX,
            flexWindowSlots = 0,
            startIndex = TimeAxis.indexOf(LocalTime.of(7, 0)),
            endIndex = TimeAxis.indexOf(LocalTime.of(7, 30))
        ),

        TimeRangeHouseholdRequirement(
            name = "朝食",
            targetState = SlotState.CHILDCARE,
            requiredCount = 1,
            allowedPersons = setOf(Person.MOTHER),
            type = RequirementType.FIX,
            flexWindowSlots = 0,
            startIndex = TimeAxis.indexOf(LocalTime.of(7, 0)),
            endIndex = TimeAxis.indexOf(LocalTime.of(7, 30))
        ),

        // 夕食準備（どちらか1人）
        TimeRangeHouseholdRequirement(
            name = "夕食準備",
            targetState = SlotState.LIFE,
            requiredCount = 1,
            allowedPersons = setOf(Person.FATHER, Person.MOTHER),
            type = RequirementType.FLEX,
            flexWindowSlots = 1,
            startIndex = TimeAxis.indexOf(LocalTime.of(18, 0)),
            endIndex = TimeAxis.indexOf(LocalTime.of(18, 30))
        ),

        // 夕食（3人で）
        TimeRangeHouseholdRequirement(
            name = "夕食",
            targetState = SlotState.CHILDCARE,
            requiredCount = 2,
            allowedPersons = setOf(Person.FATHER, Person.MOTHER),
            type = RequirementType.FLEX,
            flexWindowSlots = 1,
            startIndex = TimeAxis.indexOf(LocalTime.of(18, 30)),
            endIndex = TimeAxis.indexOf(LocalTime.of(19, 0))
        ),

        // 買い物（週3回想定 → 今はサンプルで固定）
        TimeRangeHouseholdRequirement(
            name = "買い物",
            targetState = SlotState.LIFE,
            requiredCount = 1,
            allowedPersons = setOf(Person.FATHER, Person.MOTHER),
            type = RequirementType.FLEX,
            flexWindowSlots = 3,
            startIndex = TimeAxis.indexOf(LocalTime.of(16, 0)),
            endIndex = TimeAxis.indexOf(LocalTime.of(17, 0))
        ),

        // 母の通院
        TimeRangeHouseholdRequirement(
            name = "通院",
            targetState = SlotState.LIFE,
            requiredCount = 1,
            allowedPersons = setOf(Person.MOTHER),
            type = RequirementType.FIX,
            flexWindowSlots = 0,
            startIndex = TimeAxis.indexOf(LocalTime.of(10, 0)),
            endIndex = TimeAxis.indexOf(LocalTime.of(11, 0))
        ),

        // 朝の育児 6:30 - 8:30
        TimeRangeHouseholdRequirement(
            name = "育児",
            targetState = SlotState.CHILDCARE,
            requiredCount = 1,
            allowedPersons = setOf(Person.FATHER, Person.MOTHER),
            type = RequirementType.FIX,
            flexWindowSlots = 0,
            startIndex = TimeAxis.indexOf(LocalTime.of(6, 30)),
            endIndex = TimeAxis.indexOf(LocalTime.of(7, 0))
        ),

        TimeRangeHouseholdRequirement(
            name = "育児",
            targetState = SlotState.CHILDCARE,
            requiredCount = 1,
            allowedPersons = setOf(Person.FATHER, Person.MOTHER),
            type = RequirementType.FIX,
            flexWindowSlots = 0,
            startIndex = TimeAxis.indexOf(LocalTime.of(7, 30)),
            endIndex = TimeAxis.indexOf(LocalTime.of(8, 30))
        ),

        // 夜の育児 18:00 - 20:00
        TimeRangeHouseholdRequirement(
            name = "育児",
            targetState = SlotState.CHILDCARE,
            requiredCount = 1,
            allowedPersons = setOf(Person.FATHER, Person.MOTHER),
            type = RequirementType.FIX,
            flexWindowSlots = 0,
            startIndex = TimeAxis.indexOf(LocalTime.of(18, 0)),
            endIndex = TimeAxis.indexOf(LocalTime.of(18, 30))
        ),

        TimeRangeHouseholdRequirement(
            name = "育児",
            targetState = SlotState.CHILDCARE,
            requiredCount = 1,
            allowedPersons = setOf(Person.FATHER, Person.MOTHER),
            type = RequirementType.FIX,
            flexWindowSlots = 0,
            startIndex = TimeAxis.indexOf(LocalTime.of(19, 0)),
            endIndex = TimeAxis.indexOf(LocalTime.of(20, 0))
        )
    )
}