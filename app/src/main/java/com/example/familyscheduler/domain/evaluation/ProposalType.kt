package com.example.familyscheduler.domain.evaluation

enum class ProposalType(val priority: Int) {
    WAITING(0),
    ASSIGNED(1),
    // SCHEDULEは内部のみ
}