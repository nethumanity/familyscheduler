package com.example.familyscheduler.domain.evaluation

data class ReasonEvaluation(
    val reason: MissingReason,
    val proposals: List<FlexResolveProposal>
)
