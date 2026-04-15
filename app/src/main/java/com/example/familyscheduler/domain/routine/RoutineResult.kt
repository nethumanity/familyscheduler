package com.example.familyscheduler.domain.routine

data class RoutineResult(
    val blocks: List<ChildCareBlock>,
    val events: List<ChildCareEvent>
)