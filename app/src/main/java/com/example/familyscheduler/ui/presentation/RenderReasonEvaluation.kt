package com.example.familyscheduler.ui.presentation

import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementSemantics
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.projection.WarningUiModel
import kotlin.math.abs

fun renderMissingReason(current: WarningUiModel): String {

    val blockingPersons = current.personStates.blockingPersons

    val personsText =
        if (current.personStates.requiredCount >= 2) {
            Person.entries.joinToString("・") { it.label }
        } else {
            blockingPersons.joinToString("／") { it.label }
        }

    val requirementText = current.nameText.take(15)

    val blockingPersonsText =
        when {
            blockingPersons.size >= 2 && current.personStates.requiredCount >= 2 -> {
                blockingPersons.joinToString("") { it.label } + "とも"
            }

            blockingPersons.size < 2 && current.personStates.requiredCount >= 2 -> {
                blockingPersons.joinToString("") { it.label } + "が"
            }

            else -> {
                ""
            }
        }

    return "${personsText}は${requirementText}の予定ですが、${blockingPersonsText}他の予定と重複しています"
}

fun renderFlexProposal(proposal: FlexResolveProposal): String {

    val minutes = (proposal.candidateIndex - proposal.initialIndex) * TimeAxis.stepMinutes

    val name =
        if (proposal.requirementSource.semantics == RequirementSemantics.STATE) {
            SlotStatePresentation.label(SlotState.CHILDCARE)
        } else {
            proposal.requirementName
        }

    if (minutes == 0)
        return "${name.take(15)} の担当を ${proposal.persons.single().label} に変更する"

    val direction = if (minutes > 0) "後ろに" else "前に"

    return if (proposal.requiredCount == 2) {
        val persons = proposal.persons.joinToString("・") { it.label }

        "$persons の ${name.take(15)} を ${abs(minutes)}分$direction ずらす"

    } else {
        val persons = proposal.persons.joinToString("／") { it.label }

        "$persons の ${name.take(15)} を ${abs(minutes)}分$direction ずらす"
    }
}