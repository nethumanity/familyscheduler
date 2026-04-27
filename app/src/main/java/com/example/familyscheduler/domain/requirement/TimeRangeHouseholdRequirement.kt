package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState

data class TimeRangeHouseholdRequirement(
    override val sourceRuleId: String,
    override val source: RequirementSource,
    override val name: String,
    override val targetState: SlotState,
    override val requiredCount: Int,
    override val allowedPersons: List<Person>,
    override val flexWindowSlots: FlexWindowParameters,
    val startIndex: Int,
    val endIndex: Int,
    override val prioritySeed: Long
) : HouseholdRequirement {

    override fun allIndices(): List<Int> {
        return (startIndex until endIndex).sorted().toList()
    }

    override fun isRequiredAt(index: Int): Boolean =
        index in startIndex until endIndex

    override fun requiredCountAt(index: Int): Int {
        return if (isRequiredAt(index)) requiredCount else 0
    }
}