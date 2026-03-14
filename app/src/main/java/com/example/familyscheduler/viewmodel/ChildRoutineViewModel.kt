package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.repository.ChildOverrideRepository
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ChildRoutineViewModel(
    private val repository: ChildRoutineRepository,
    private val overrideRepository: ChildOverrideRepository
) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildRoutineInput>>(emptyList())
    val children = _children.asStateFlow()

    private val _overrides =
        MutableStateFlow<Map<Pair<String, LocalDate>, ChildTodayRoutine>>(emptyMap())

    val overrides: StateFlow<Map<Pair<String, LocalDate>, ChildTodayRoutine>> =
        _overrides

    init {
        viewModelScope.launch {
            _children.value = repository.getAll()
            //_overrides.value = overrideRepository.getAll()    //←必要ですか？
        }
    }

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
        _uiState.update { state ->
            state.copy(
                nurseryStart = time,
                nurseryStartEarliest =
                    if (state.nurseryStartEarliest == state.nurseryStart)
                        time
                    else
                        state.nurseryStartEarliest,
                nurseryStartLatest =
                    if (state.nurseryStartLatest == state.nurseryStart)
                        time
                    else
                        state.nurseryStartLatest
            )
        }
    }

    /* 旧バージョン（どっちがいい？）
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
     */

    fun updateNurseryStartEarliest(time: LocalTime) =
        _uiState.update { it.copy(nurseryStartEarliest = time) }

    fun updateNurseryStartLatest(time: LocalTime) =
        _uiState.update { it.copy(nurseryStartLatest = time) }

    fun updateNurseryEnd(time: LocalTime) {
        _uiState.update { state ->
            state.copy(
                nurseryEnd = time,
                nurseryEndEarliest =
                    if (state.nurseryEndEarliest == state.nurseryEnd)
                        time
                    else
                        state.nurseryEndEarliest,
                nurseryEndLatest =
                    if (state.nurseryEndLatest == state.nurseryEnd)
                        time
                    else
                        state.nurseryEndLatest
            )
        }
    }

    /* 旧バージョン（どっちがいい？）
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
     */

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

            _children.value = repository.getAll()   //_children.update {it + routine}どっちがいい？

            _saveCompleted.emit(Unit)
        }
    }

    fun convertToChildRoutine(
        state: ChildRoutineUiState
    ): ChildRoutineInput {

        val wakeUp = requireNotNull(state.wakeUpTime)
        val sleep = requireNotNull(state.sleepTime)

        val start = state.nurseryStart ?: LocalTime.NOON
        val end = state.nurseryEnd ?: start

        val startEarliest =
            (state.nurseryStartEarliest ?: start)
                .coerceAtMost(start)

        val startLatest =
            (state.nurseryStartLatest ?: start)
                .coerceAtLeast(start)

        val endEarliest =
            (state.nurseryEndEarliest ?: end)
                .coerceAtMost(end)

        val endLatest =
            (state.nurseryEndLatest ?: end)
                .coerceAtLeast(end)

        return ChildRoutineInput(
            name = state.name,
            wakeUpTime = wakeUp,
            sleepTime = sleep,
            daysOfWeek = if (state.hasNursery) state.daysOfWeek else emptySet(),
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

        val wakeUpTime: LocalTime = LocalTime.of(7, 0), //? = null,
        val sleepTime: LocalTime = LocalTime.of(21, 0), //? = null,

        val hasNursery: Boolean = true,

        val daysOfWeek: Set<DayOfWeek> =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ),

        val nurseryStart: LocalTime = LocalTime.of(8, 0), //? = null,
        val nurseryStartEarliest: LocalTime? = null,
        val nurseryStartLatest: LocalTime? = null,

        val nurseryEnd: LocalTime = LocalTime.of(17, 0), //? = null,
        val nurseryEndEarliest: LocalTime? = null,
        val nurseryEndLatest: LocalTime? = null
    )

    fun toggleTodayRoutine(
        child: ChildRoutineInput,
        date: LocalDate
    ) {
        viewModelScope.launch {

            val current = resolveTodayRoutine(child, date, _overrides.value)

            val next = current.next(child)

            Log.d("override", "current=$current next=$next")

            overrideRepository.saveOverride(
                child.name,
                date,
                next
            )

            _overrides.value = overrideRepository.getAll()
        }
    }

    fun resolveTodayRoutine(
        child: ChildRoutineInput,
        date: LocalDate,
        overrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>
    ): ChildTodayRoutine {

        overrides[child.name to date]?.let {
            Log.d("override", "override found $it")
            return it
        }

        val day = date.dayOfWeek

        return if (day in child.daysOfWeek) {
            ChildTodayRoutine.NURSERY
        } else {
            ChildTodayRoutine.HOME
        }
    }
}