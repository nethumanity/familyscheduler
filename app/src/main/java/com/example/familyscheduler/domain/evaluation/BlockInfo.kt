package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState

data class BlockInfo(
    val person: List<Person>,
    val currentState: List<SlotState>,
    val taskIds: List<String>
)
