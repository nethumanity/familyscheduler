package com.example.familyscheduler.domain.model

import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalTime

enum class DailyState(val label: String) {
    OFFICE("出勤"),
    REMOTE("在宅"),
    OVERTIME("残業"),
    OFF("休暇");

    fun dailyStateLabel(state: DailyState?): String =
        state?.label ?: ""

    fun next(): DailyState =
        values()[(ordinal + 1) % values().size]

    fun defaultSlotStateAt(index: Int): SlotState {
        return when (this) {
            OFFICE ->
                if (index in TimeAxis.indexOf(LocalTime.of(9, 0))until TimeAxis.indexOf(LocalTime.of(18, 0)))
                    SlotState.WORK
                else if (index in TimeAxis.indexOf(LocalTime.of(8, 0))until TimeAxis.indexOf(LocalTime.of(9, 0)))
                    SlotState.UNAVAILABLE
                else if (index in TimeAxis.indexOf(LocalTime.of(18, 0))until TimeAxis.indexOf(LocalTime.of(19, 0)))
                    SlotState.UNAVAILABLE
                else if (index in TimeAxis.indexOf(LocalTime.of(5, 0))until TimeAxis.indexOf(LocalTime.of(6, 0)))
                        SlotState.REST
                else if (index in TimeAxis.indexOf(LocalTime.of(22, 0)).. TimeAxis.indexOf(LocalTime.of(23, 30)))
                    SlotState.REST
                else
                    SlotState.UNASSIGNED

            REMOTE ->
                if (index in TimeAxis.indexOf(LocalTime.of(9, 0))until TimeAxis.indexOf(LocalTime.of(18, 0)))
                    SlotState.WORK
                else if (index in TimeAxis.indexOf(LocalTime.of(5, 0))until TimeAxis.indexOf(LocalTime.of(6, 0)))
                    SlotState.REST
                else if (index in TimeAxis.indexOf(LocalTime.of(22, 0)).. TimeAxis.indexOf(LocalTime.of(23, 30)))
                    SlotState.REST
                else
                    SlotState.UNASSIGNED

            OFF ->
                if (index in TimeAxis.indexOf(LocalTime.of(5, 0))until TimeAxis.indexOf(LocalTime.of(6, 0)))
                    SlotState.REST
                else if (index in TimeAxis.indexOf(LocalTime.of(22, 0)).. TimeAxis.indexOf(LocalTime.of(23, 30)))
                    SlotState.REST
                else
                    SlotState.UNASSIGNED

            else ->
                SlotState.UNASSIGNED
        }
    }
}
