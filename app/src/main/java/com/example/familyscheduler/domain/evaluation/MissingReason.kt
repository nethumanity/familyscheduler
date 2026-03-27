package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.ui.utilities.slotStateLabel

sealed class MissingReason {
    data class NotEnoughPeople(
        val sourceRuleId: String,
        val requirementName: String,
        val requiredCount: Int,
        val assignedCount: Int,
        val blockingPersons: BlockInfo    //Listをやめて検証中
    ) : MissingReason()

    data class NoAssignablePerson(
        val requirementName: String
    ) : MissingReason()

    data class StateConflict(
        val person: Person,
        val expected: SlotState,
        val actual: SlotState
    ) : MissingReason()

    companion object {
        fun renderMissingReason(reason: MissingReason): String =
            when (reason) {

                is NotEnoughPeople -> {

                    val block = reason.blockingPersons

                    val personsText =
                        block.person.joinToString("、") { it.label }

                    val statesText =
                        block.currentState.joinToString("、") { slotStateLabel(it) }

                    "${personsText}に${reason.requirementName}の予定がありますが、すでに${statesText}が入っています"
                }

                is NoAssignablePerson ->
                    "${reason.requirementName}：割り当て可能な人がいません"

                is StateConflict ->
                    "${reason.person.label}は ${reason.actual} のため対応できません（必要: ${reason.expected}）"
            }

        fun renderMissingReasonSummary(reason: MissingReason): String {

            return when (reason) {
                is NotEnoughPeople ->
                    "${reason.requirementName}：${reason.assignedCount}/${reason.requiredCount}"
                is NoAssignablePerson -> "状態不一致（未設定）"
                is StateConflict -> "担当不可（未設定）"
            }
        }

        fun renderMissingReasonCount(reason: MissingReason): String {

            return when (reason) {
                is NotEnoughPeople ->
                    "${reason.assignedCount}/${reason.requiredCount}"
                is NoAssignablePerson -> "状態不一致（未設定）"
                is StateConflict -> "担当不可（未設定）"
            }
        }
    }
}

data class BlockInfo(
    val person: List<Person>,
    val currentState: List<SlotState>,
    val taskName: List<String?>
)
