package com.example.familyscheduler.domain.evaluation

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import kotlin.math.abs

data class FlexResolveProposal(
    val requirementName: String,    //絞り込み条件を確認
    val persons: List<Person>,             //Personから変更、Set?
    val initialIndex: Int,          //reqIndexとかがいい？
    val candidateIndex: Int,
    //val deltaMinutes: Int,          //いらない？
    val targetState: SlotState
) {
    fun score(slots: List<TimeSlot>): Int {     //検証中
        val candidateSlot = slots.find {
            it.person in persons && it.index == candidateIndex
        } ?: return Int.MAX_VALUE

        val moveCost =
            if (candidateSlot.state == targetState) 0
            else candidateSlot.state.weight

        val distanceCost = abs(candidateIndex - initialIndex)

        return moveCost * 10 + distanceCost
    }
}