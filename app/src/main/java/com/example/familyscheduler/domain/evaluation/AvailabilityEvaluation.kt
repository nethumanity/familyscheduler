package com.example.familyscheduler.domain.evaluation

data class AvailabilityEvaluation(      //ネスト構造を要検討
    val index: Int,
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
