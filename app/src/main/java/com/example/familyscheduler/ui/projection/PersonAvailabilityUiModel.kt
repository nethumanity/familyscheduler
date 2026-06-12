package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.person.Person

data class PersonAvailabilityUiModel(
    val assignablePersons: List<Person>,
    val blockingPersons: List<Person>,
    val requiredCount: Int
)