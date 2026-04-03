package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.repository.ChildOverrideRepository
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.ui.utilities.EditingTarget
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ChildRoutineViewModel(
    private val repository: ChildRoutineRepository,
    private val overrideRepository: ChildOverrideRepository
) : ViewModel() {

    data class ChildRoutineScreenState(
        val routines: List<ChildRoutineInput> = emptyList(),
        val overrides: Map<Pair<String, LocalDate>, ChildTodayRoutine> = emptyMap(),
        val form: ChildRoutineUiState = ChildRoutineUiState()
    )

    val childRoutines = repository.getAllFlow()
    val overrides = overrideRepository.getAllFlow()

    private val _editingTarget = MutableStateFlow<EditingTarget?>(null)
    val editingTarget: StateFlow<EditingTarget?> = _editingTarget

    private val _formState = MutableStateFlow(ChildRoutineUiState())

    val uiState =
        combine(
            childRoutines,
            overrides,
            _formState
        ) { routines, overrides, form ->
            ChildRoutineScreenState(
                routines = routines,
                overrides = overrides,
                form = form
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ChildRoutineScreenState()
        )

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted = _saveCompleted.asSharedFlow()

    fun updateName(name: String) =
        _formState.update { it.copy(name = name) }

    fun updateWakeUpTime(time: LocalTime) =
        _formState.update { it.copy(wakeUpTime = time) }

    fun updateSleepTime(time: LocalTime) =
        _formState.update { it.copy(sleepTime = time) }

    fun updateHasNursery(value: Boolean) =
        _formState.update {
            it.copy(
                hasNursery = value,
                daysOfWeek = if (value) it.daysOfWeek else emptySet()
            )
        }

    fun updateNurseryStart(time: LocalTime) {
        _formState.update { state ->
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

    fun updateNurseryStartEarliest(time: LocalTime) =
        _formState.update { it.copy(nurseryStartEarliest = time) }

    fun updateNurseryStartLatest(time: LocalTime) =
        _formState.update { it.copy(nurseryStartLatest = time) }

    fun updateNurseryEnd(time: LocalTime) {
        _formState.update { state ->
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

    fun updateNurseryEndEarliest(time: LocalTime) =
        _formState.update { it.copy(nurseryEndEarliest = time) }

    fun updateNurseryEndLatest(time: LocalTime) =
        _formState.update { it.copy(nurseryEndLatest = time) }

    fun toggleDay(day: DayOfWeek) {
        _formState.update { state ->
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

    fun isValid(form: ChildRoutineUiState): Boolean {
        return if (form.hasNursery) {
            form.name.isNotBlank() &&
                    form.daysOfWeek.isNotEmpty()
        } else {
            form.name.isNotBlank()
        }
    }

    fun onSave() {

        val routine = convertToChildRoutine(_formState.value)

        viewModelScope.launch {

            repository.save(routine)

            _formState.value = ChildRoutineUiState() // ← 追加
            _saveCompleted.emit(Unit)

            Log.d("RoutineSave", "Saved routine: $routine")
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

    data class ChildRoutineUiState(

        val name: String = "",

        val wakeUpTime: LocalTime = LocalTime.of(7, 0),
        val sleepTime: LocalTime = LocalTime.of(21, 0),

        val hasNursery: Boolean = true,

        val daysOfWeek: Set<DayOfWeek> =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ),

        val nurseryStart: LocalTime = LocalTime.of(8, 0),
        val nurseryStartEarliest: LocalTime? = null,
        val nurseryStartLatest: LocalTime? = null,

        val nurseryEnd: LocalTime = LocalTime.of(17, 0),
        val nurseryEndEarliest: LocalTime? = null,
        val nurseryEndLatest: LocalTime? = null
    )

    // 挙動確認後、削除
    fun resetUiState() {
        _formState.value = ChildRoutineUiState()
    }

    fun load(childName: String) {

        viewModelScope.launch {

            val child = repository.getByChildName(childName).first() ?: return@launch

            _formState.update {
                ChildRoutineUiState(
                    name = child.name,

                    wakeUpTime = child.wakeUpTime,
                    sleepTime = child.sleepTime,

                    hasNursery = child.daysOfWeek.isNotEmpty(),

                    daysOfWeek = child.daysOfWeek,

                    nurseryStart = child.nurseryStart,
                    nurseryStartEarliest = child.nurseryStartEarliest,
                    nurseryStartLatest = child.nurseryStartLatest,

                    nurseryEnd = child.nurseryEnd,
                    nurseryEndEarliest = child.nurseryEndEarliest,
                    nurseryEndLatest = child.nurseryEndLatest
                )
            }
        }
    }

    fun clearEditingTarget() {
        _editingTarget.value = null
    }

    fun toggleTodayRoutine(
        child: ChildRoutineInput,
        date: LocalDate
    ) {
        viewModelScope.launch {

            val current = resolveTodayRoutine(child, date, uiState.value.overrides)

            val next = current.next(child)

            Log.d("override", "current=$current next=$next")

            overrideRepository.saveOverride(
                child.name,
                date,
                next
            )
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

    fun startEditChildRoutine(childName: String) {

        if (_editingTarget.value != null) return

        _editingTarget.value = EditingTarget(
            childRoutineId = childName
        )
    }

    fun deleteChildRoutine(childName: String) {
        viewModelScope.launch {
            overrideRepository.deleteByChildName(childName)
            repository.delete(childName)

            // 編集中なら解除
            if (_editingTarget.value?.childRoutineId == childName) {
                _editingTarget.value = null
            }

            // UI通知（任意）
            //_deleteCompleted.emit(Unit)
        }
    }
}