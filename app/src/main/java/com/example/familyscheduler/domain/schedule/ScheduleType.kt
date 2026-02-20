package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.slot.SlotState
import java.util.UUID

data class ScheduleType(    //SloStateへの変換責任
    val id: UUID,
    val title: String,    //ユーザーの認識のためだが、基本は「仕事」「睡眠」「往路通勤（準備や途中の用事を含む）」「復路通勤（片付けや途中の用事を含む）」だけにする
    val category: StateCategory,
    val flexWindow: Int = 3
) {
    fun toSlotState(): SlotState =
        when (category) {
            StateCategory.WORK -> SlotState.WORK
            StateCategory.REST -> SlotState.REST
            StateCategory.BLOCKED -> SlotState.UNAVAILABLE
        }
}

enum class StateCategory {
    WORK,
    REST,
    BLOCKED // UNAVAILABLE系
}