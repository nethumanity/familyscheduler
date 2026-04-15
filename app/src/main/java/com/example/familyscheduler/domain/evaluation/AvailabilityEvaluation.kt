package com.example.familyscheduler.domain.evaluation

data class AvailabilityEvaluation(
    val index: Int,
    val hasFlexRequirement: Boolean,
    val missing: Int,
    val reasons: List<ReasonEvaluation>
) {
    val state: AvailabilityState
        get() = when {
            reasons.isEmpty() -> AvailabilityState.OK
            else -> AvailabilityState.WARN
    }
}
