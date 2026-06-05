package com.example.familyscheduler.domain.evaluation

data class AvailabilityEvaluation(
    val index: Int,
    val warningReqIds: List<String>
) {
    val state: AvailabilityState
        get() = when {
            warningReqIds.isEmpty() -> AvailabilityState.OK
            else -> AvailabilityState.WARN
    }
}
