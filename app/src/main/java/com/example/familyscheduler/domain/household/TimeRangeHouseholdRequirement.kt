package com.example.familyscheduler.domain.household

import com.example.familyscheduler.domain.model.Person
import com.example.familyscheduler.domain.model.RequirementType
import com.example.familyscheduler.domain.model.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalTime

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

/*
fun range(
    start: LocalTime,
    end: LocalTime
): IntRange {
    return TimeAxis.indexOf(start)until TimeAxis.indexOf(end)
}
 */