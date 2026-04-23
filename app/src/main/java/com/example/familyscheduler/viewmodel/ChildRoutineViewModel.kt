package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.RoutineOverrideSnapshot
import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import com.example.familyscheduler.domain.routine.repository.RoutineToggleOverrideRepository
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.domain.routine.repository.RoutineShiftOverrideRepository
import com.example.familyscheduler.ui.utilities.ChildRoutineUndoPayload
import com.example.familyscheduler.ui.utilities.EditingTarget
import com.example.familyscheduler.ui.utilities.UiEvent
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
import java.util.UUID

class ChildRoutineViewModel(
    private val repository: ChildRoutineRepository,
    private val routineToggleOverrideRepository: RoutineToggleOverrideRepository,
    private val routineShiftOverrideRepository: RoutineShiftOverrideRepository
) : ViewModel() {

    data class ChildRoutineScreenState(
        val routines: List<ChildRoutineInput> = emptyList(),
        val overrides: Map<Pair<String, LocalDate>, ChildTodayRoutine> = emptyMap(),
        val shiftOverrides: List<RoutineShiftOverride> = emptyList(),
        val form: ChildRoutineUiState = ChildRoutineUiState()
    )

    val childRoutines = repository.getAllFlow()
    val toggleOverrides = routineToggleOverrideRepository.getAllFlow()
    val shiftOverrides = routineShiftOverrideRepository.getAllFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private val _editingTarget = MutableStateFlow<EditingTarget?>(null)
    val editingTarget: StateFlow<EditingTarget?> = _editingTarget

    private val _formState = MutableStateFlow(ChildRoutineUiState())

    val uiState =
        combine(
            childRoutines,
            toggleOverrides,
            shiftOverrides,
            _formState
        ) { routines, overrides, shiftOverrides, form ->
            ChildRoutineScreenState(
                routines = routines,
                overrides = overrides,
                shiftOverrides = shiftOverrides,
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

            _formState.value = ChildRoutineUiState()
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
            childId = state.id ?: UUID.randomUUID().toString(),
            childName = state.name.trim(),
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
        val id: String? = null,
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

    fun load(childId: String) {

        viewModelScope.launch {

            val routine = repository.getByChildId(childId).first() ?: return@launch

            _formState.update {
                ChildRoutineUiState(
                    id = routine.childId,
                    name = routine.childName,
                    wakeUpTime = routine.wakeUpTime,
                    sleepTime = routine.sleepTime,

                    hasNursery = routine.daysOfWeek.isNotEmpty(),
                    daysOfWeek = routine.daysOfWeek,

                    nurseryStart = routine.nurseryStart,
                    nurseryStartEarliest = routine.nurseryStartEarliest,
                    nurseryStartLatest = routine.nurseryStartLatest,

                    nurseryEnd = routine.nurseryEnd,
                    nurseryEndEarliest = routine.nurseryEndEarliest,
                    nurseryEndLatest = routine.nurseryEndLatest
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

            routineToggleOverrideRepository.replace(
                child.childId,
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

        overrides[child.childId to date]?.let {
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

    fun startEditChildRoutine(childId: String) {

        if (_editingTarget.value != null) return

        _editingTarget.value = EditingTarget(
            childRoutineId = childId
        )
    }

    fun deleteChildRoutine(childId: String) {

        val routine = uiState.value.routines
            .find { it.childId == childId }
            ?: return

        val snapshot = collectOverridesForChild(childId)

        val payload = ChildRoutineUndoPayload(
            routine = routine,
            snapshot = snapshot
        )

        viewModelScope.launch {
            routineToggleOverrideRepository.deleteAllByChildId(childId)
            routineShiftOverrideRepository.deleteAllByChildId(childId)
            repository.delete(childId)

            // 編集中なら解除
            if (_editingTarget.value?.childRoutineId == childId) {
                _editingTarget.value = null
            }

            _events.emit(
                UiEvent.ShowUndoDelete(
                    onUndo = { undoDeleteChildRoutine(payload) }
                )
            )

            // UI通知（任意）
            //_deleteCompleted.emit(Unit)
        }
    }

    fun undoDeleteChildRoutine(payload: ChildRoutineUndoPayload) {

        viewModelScope.launch {
            repository.save(payload.routine)

            payload.snapshot.childOverrides.forEach {
                routineToggleOverrideRepository.replace(
                    it.key.first,
                    it.key.second,
                    it.value
                )
            }

            payload.snapshot.shiftOverrides.forEach {
                routineShiftOverrideRepository.replace(it)
            }
        }
    }

    fun collectOverridesForChild(childId: String): RoutineOverrideSnapshot {
        val childOverrides = uiState.value.overrides
            .filterKeys { it.first == childId }

        val shiftOverrides = uiState.value.shiftOverrides
            .filter { it.childId == childId }

        return RoutineOverrideSnapshot(childOverrides, shiftOverrides)
    }
}