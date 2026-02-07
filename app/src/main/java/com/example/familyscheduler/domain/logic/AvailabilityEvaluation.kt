package com.example.familyscheduler.domain.logic

import com.example.familyscheduler.MainViewModel
import com.example.familyscheduler.domain.model.AvailabilityState

data class AvailabilityEvaluation(
    val index: Int,
    val requiredCount: Int,
    val availableCount: Int,
    val hasFixRequirement: Boolean,
    val hasFlexRequirement: Boolean,
    val missing: Int,
    val reasons: List<MissingReason>,
    val flexProposals: List<MainViewModel.FlexResolveProposal> = emptyList()
) {
    val state: AvailabilityState
        get() = when {
            reasons.isEmpty() -> AvailabilityState.OK
            else -> AvailabilityState.WARN
    }
}
