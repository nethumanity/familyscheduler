package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState

interface HouseholdRequirement {
    val name: String
    val targetState: SlotState
    val requiredCount: Int
    val allowedPersons: Set<Person>
    val flexWindowSlots: FlexWindowParameters
    val prioritySeed: Long

    fun isRequiredAt(index: Int): Boolean

    fun requiredCountAt(index: Int): Int

}
