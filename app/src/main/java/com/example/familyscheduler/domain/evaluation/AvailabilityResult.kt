package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.slot.TimeSlot

data class AvailabilityResult(
    val slots: List<TimeSlot>,
    val evaluations: List<AvailabilityEvaluation>,
    val proposalsByRequirementId: Map<String, List<FlexResolveProposal>>
)