package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState

interface HouseholdRequirement {
    val name: String                  // 表示用ラベル（朝食、通院など）
    val targetState: SlotState         // このRequirementが満たす意味的state
    val requiredCount: Int             // 同時に何人必要か
    val allowedPersons: Set<Person>    // 担当可能な人
    val type: RequirementType          // FIX / FLEX
    val flexWindowSlots: Int           // FLEX用（FIXなら0）

    fun isRequiredAt(index: Int): Boolean


    fun requiredCountAt(index: Int): Int

}
