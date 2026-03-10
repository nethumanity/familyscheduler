package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.requirement.ChildRoutineInput
import com.example.familyscheduler.domain.requirement.repository.ChildRoutineRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class ChildRoutineViewModel(
    private val repository: ChildRoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildRoutineUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted = _saveCompleted.asSharedFlow()

    fun updateName(name: String) =
        _uiState.update { it.copy(name = name) }

    fun updateWakeUpTime(time: LocalTime) =
        _uiState.update { it.copy(wakeUpTime = time) }

    fun updateSleepTime(time: LocalTime) =
        _uiState.update { it.copy(sleepTime = time) }

    fun updateHasNursery(value: Boolean) =
        _uiState.update {
            it.copy(
                hasNursery = value,
                daysOfWeek = if (value) it.daysOfWeek else emptySet()
            )
        }

    fun updateNurseryStart(time: LocalTime) {

        _uiState.update {

            it.copy(
                nurseryStart = time,
                nurseryStartEarliest =
                    it.nurseryStartEarliest ?: time,
                nurseryStartLatest =
                    it.nurseryStartLatest ?: time
            )
        }
    }

    fun updateNurseryStartEarliest(time: LocalTime) =
        _uiState.update { it.copy(nurseryStartEarliest = time) }

    fun updateNurseryStartLatest(time: LocalTime) =
        _uiState.update { it.copy(nurseryStartLatest = time) }

    fun updateNurseryEnd(time: LocalTime) {

        _uiState.update {

            it.copy(
                nurseryEnd = time,
                nurseryEndEarliest =
                    it.nurseryEndEarliest ?: time,
                nurseryEndLatest =
                    it.nurseryEndLatest ?: time
            )
        }
    }

    fun updateNurseryEndEarliest(time: LocalTime) =
        _uiState.update { it.copy(nurseryEndEarliest = time) }

    fun updateNurseryEndLatest(time: LocalTime) =
        _uiState.update { it.copy(nurseryEndLatest = time) }

    fun toggleDay(day: DayOfWeek) {
        _uiState.update { state ->
            val newDays =
                if (day in state.daysOfWeek)
                    state.daysOfWeek - day
                else
                    state.daysOfWeek + day

            state.copy(
                daysOfWeek = newDays
            )
        }
    }

    fun onSave() {

        val routine = convertToChildRoutine(_uiState.value)

        viewModelScope.launch {

            repository.add(routine)

            Log.d("RoutineSave", "Saved routine: $routine")

            _saveCompleted.emit(Unit)
        }
    }

    fun convertToChildRoutine(state: ChildRoutineUiState
    ): ChildRoutineInput {

        val wakeUp = requireNotNull(state.wakeUpTime)
        val sleep = requireNotNull(state.sleepTime)

        // いらない？
        val nurseryDay: Set<DayOfWeek> =
            if (state.hasNursery) {
                state.daysOfWeek
            } else {
                emptySet()
            }

        val DUMMY_TIME = LocalTime.NOON

        val start =
            if (state.hasNursery) {
                requireNotNull(state.nurseryStart)
            } else DUMMY_TIME
        val startEarliest =
            if (state.hasNursery) {
                requireNotNull(state.nurseryStartEarliest)
            } else DUMMY_TIME
        val startLatest =
            if (state.hasNursery) {
                requireNotNull(state.nurseryStartLatest)
            } else DUMMY_TIME
        val end =
            if (state.hasNursery) {
                requireNotNull(state.nurseryEnd)
            } else DUMMY_TIME
        val endEarliest =
            if (state.hasNursery) {
                requireNotNull(state.nurseryEndEarliest)
            } else DUMMY_TIME
        val endLatest =
            if (state.hasNursery) {
                requireNotNull(state.nurseryEndLatest)
            } else DUMMY_TIME

        return ChildRoutineInput(
            name = state.name,
            wakeUpTime = wakeUp,
            sleepTime = sleep,
            daysOfWeek = nurseryDay,
            nurseryStart = start,
            nurseryStartEarliest = startEarliest,
            nurseryStartLatest = startLatest,
            nurseryEnd = end,
            nurseryEndEarliest = endEarliest,
            nurseryEndLatest = endLatest
        )
    }

    val dayLabels = mapOf(
        DayOfWeek.MONDAY to "月",
        DayOfWeek.TUESDAY to "火",
        DayOfWeek.WEDNESDAY to "水",
        DayOfWeek.THURSDAY to "木",
        DayOfWeek.FRIDAY to "金",
        DayOfWeek.SATURDAY to "土",
        DayOfWeek.SUNDAY to "日"
    )

    data class ChildRoutineUiState(

        val name: String = "",

        val wakeUpTime: LocalTime? = null,
        val sleepTime: LocalTime? = null,

        val hasNursery: Boolean = true,

        val daysOfWeek: Set<DayOfWeek> =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ),

        val nurseryStart: LocalTime? = null,
        val nurseryStartEarliest: LocalTime? = null,
        val nurseryStartLatest: LocalTime? = null,

        val nurseryEnd: LocalTime? = null,
        val nurseryEndEarliest: LocalTime? = null,
        val nurseryEndLatest: LocalTime? = null
    )
}