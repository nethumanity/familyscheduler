package com.example.familyscheduler.ui.projection

import com.example.familyscheduler.domain.person.Person

data class PersonAvailabilityUiModel(
    val blockingPersons: List<Person>,
    val requiredCount: Int
)