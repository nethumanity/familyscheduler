package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.proposal.FlexResolveProposal

data class AvailabilityEvaluation(
    val index: Int,
    val requiredCount: Int,
    val availableCount: Int,
    val hasFixRequirement: Boolean,
    val hasFlexRequirement: Boolean,
    val missing: Int,
    val reasons: List<MissingReason>,
    val flexProposals: List<FlexResolveProposal> = emptyList()
) {
    val state: AvailabilityState
        get() = when {
            reasons.isEmpty() -> AvailabilityState.OK
            else -> AvailabilityState.WARN
    }
}
