package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.person.Person

data class PersonAvailabilityUiModel(
    val person: Person,
    val assignable: Boolean
)