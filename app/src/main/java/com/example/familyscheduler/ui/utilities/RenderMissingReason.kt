package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.domain.evaluation.MissingReason
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.time.TimeAxis

fun renderBlockingPersons(reason: MissingReason): String {
    val persons = reason.blockingPersons.person   // List<Person>

    val personSet = persons.toSet()

    return when {

        reason.requiredCount == 1 -> {
            persons.joinToString(" ") { "✖${it.label}" }
        }

        reason.requiredCount == 2 -> {
            val father = if (Person.FATHER in personSet) "✖父" else "○父"
            val mother = if (Person.MOTHER in personSet) "✖母" else "○母"
            "$father $mother"
        }

        else -> ""
    }
}

fun renderMissingReason(reason: MissingReason): String {

    val block = reason.blockingPersons

    val personsText =
        block.person.joinToString("、") { it.label }

    val statesText =
        block.currentState.joinToString("、") { slotStateLabel(it) }

    return "${personsText}に${reason.requirementName}の予定がありますが、すでに${statesText}が入っています"
}

fun renderMissingReasonCount(reason: MissingReason): String =
    "${reason.assignedCount}/${reason.requiredCount}"

fun renderFlexProposal(proposal: FlexResolveProposal): String {
    val persons = proposal.persons.joinToString("・") { it.name }

    val minutes =
        (proposal.candidateIndex - proposal.initialIndex) *
                TimeAxis.stepMinutes

    val direction = if (minutes > 0) "後ろに" else "前に"

    return "$persons の ${proposal.requirementName} を ${kotlin.math.abs(minutes)}分$direction ずらす"
}