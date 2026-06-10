package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

data class HouseholdRequirementRule(
    val id: String = UUID.randomUUID().toString(),
    val source: RequirementSource = RequirementSource.USER,
    val taskName: String,
    val targetState: SlotState,
    val requiredCount: Int,
    val allowedPersons: List<Person>,
    val flexWindowSlots: FlexWindowParameters,
    val date: LocalDate?,
    val daysOfWeek: Set<DayOfWeek>?,
    val timeRange: TimeRange,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toRequirement(
        requiredCount: Int = this.requiredCount
    ): TimeRangeHouseholdRequirement {

        val startIndex = TimeAxis.indexOf(timeRange.start)
        val endIndex = TimeAxis.indexOf(timeRange.end)

        return TimeRangeHouseholdRequirement(
            sourceRuleId = id,
            source = source,
            name = taskName,
            targetState = targetState,
            requiredCount = requiredCount,
            allowedPersons = allowedPersons,
            flexWindowSlots = flexWindowSlots,
            startIndex = startIndex,
            endIndex = endIndex,
            prioritySeed = this.prioritySeed(startIndex, endIndex)
        )
    }

    fun prioritySeed(startIndex: Int, endIndex: Int): Long {
        val flex = flexWindowSlots.backward + flexWindowSlots.forward
        val length = endIndex - startIndex
        val tightness =
            if (allowedPersons.isEmpty()) 1.0
            else 2.0 / (allowedPersons.size * requiredCount)

        val constraintScore =
            (tightness * 1_000_000).toLong()

        val semanticScore =
            targetState.weight * 100_000L

        val sourceScore =
            when (source.semantics) {
                RequirementSemantics.TASK -> 60_000L
                RequirementSemantics.EVENT -> 30_000L
                RequirementSemantics.STATE -> 0L
            }

        val timeScore =
            length * 10_000L

        val flexibleScore =
            (10_000L / (1 + flex))

        val tieBreaker =
            ((if (date != null) 1 else 0) * 1_000L) + (createdAt % 1000)

        return constraintScore +
                semanticScore +
                sourceScore +
                timeScore +
                flexibleScore +
                tieBreaker
    }
}