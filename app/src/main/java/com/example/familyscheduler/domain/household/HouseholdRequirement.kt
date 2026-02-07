package com.example.familyscheduler.domain.household

import com.example.familyscheduler.domain.model.Person
import com.example.familyscheduler.domain.model.RequirementType
import com.example.familyscheduler.domain.model.SlotState

interface HouseholdRequirement {
    val name: String                  // 表示用ラベル（朝食、通院など）
    val targetState: SlotState         // このRequirementが満たす意味的state
    val requiredCount: Int             // 同時に何人必要か
    val allowedPersons: Set<Person>    // 担当可能な人
    val type: RequirementType          // FIX / FLEX
    val flexWindowSlots: Int           // FLEX用（FIXなら0）

    fun isRequiredAt(index: Int): Boolean


    fun requiredCountAt(index: Int): Int

    /*
    fun HouseholdRequirement.movableIndicesFor(index: Int): IntRange {
        return FlexCalculator.movableIndices(
            currentIndex = index,
            flexWindowSlots = flexWindowSlots
        )
    }

    fun flexWindowAt(index: Int): Int

     */
}
