package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState

class TimeRangeHouseholdRequirement(
    override val name: String,
    override val targetState: SlotState,
    override val requiredCount: Int,
    override val allowedPersons: Set<Person>,
    override val type: RequirementType,
    override val flexWindowSlots: Int,
    private val startIndex: Int,
    private val endIndex: Int
) : HouseholdRequirement {

    override fun isRequiredAt(index: Int): Boolean =
        index in startIndex until endIndex



    override fun requiredCountAt(index: Int): Int {
        return if (isRequiredAt(index)) requiredCount else 0
    }

}