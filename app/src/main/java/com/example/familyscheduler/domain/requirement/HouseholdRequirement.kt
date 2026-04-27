package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState

interface HouseholdRequirement {
    val sourceRuleId: String
    val source: RequirementSource
    val name: String
    val targetState: SlotState
    val requiredCount: Int
    val allowedPersons: List<Person>
    val flexWindowSlots: FlexWindowParameters
    val prioritySeed: Long

    fun allIndices(): List<Int>

    fun isRequiredAt(index: Int): Boolean

    fun requiredCountAt(index: Int): Int

}
