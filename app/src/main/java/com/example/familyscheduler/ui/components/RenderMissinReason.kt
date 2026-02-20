package com.example.familyscheduler.ui.components

import com.example.familyscheduler.domain.evaluation.MissingReason

fun renderMissingReason(reason: MissingReason): String =
    when (reason) {

        is MissingReason.NotEnoughPeople ->

            reason.blockingPersons.joinToString("\n") { block ->

                val personsText =
                    block.person.joinToString("、") { it.label }

                val statesText =
                    block.currentState.joinToString("、") { slotStateLabel(it) }

                "${personsText}に${block.taskName}の予定がありますが、すでに${statesText}が入っています"
            }

        is MissingReason.NoAssignablePerson ->
            "${reason.requirementName}：割り当て可能な人がいません"

        is MissingReason.StateConflict ->
            "${reason.person.label}は ${reason.actual} のため対応できません（必要: ${reason.expected}）"
    }