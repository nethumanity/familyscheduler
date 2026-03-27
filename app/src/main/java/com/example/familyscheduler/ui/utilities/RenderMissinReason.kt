package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.evaluation.MissingReason
import com.example.familyscheduler.domain.person.Person

fun renderBlockingPersons(reason: MissingReason.NotEnoughPeople): String {
    val persons = reason.blockingPersons.person   // List<Person>

    val personSet = persons.toSet()

    return when {
        // requiredCount == 1
        reason.requiredCount == 1 -> {
            persons.joinToString(" ") { "✖${it.label}" }
        }

        // requiredCount == 2
        reason.requiredCount == 2 -> {
            val father = if (Person.FATHER in personSet) "✖父" else "○父"
            val mother = if (Person.MOTHER in personSet) "✖母" else "○母"
            "$father $mother"
        }

        else -> ""
    }
}