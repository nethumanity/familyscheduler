package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.AllowedPersonOption
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

class WeeklyTaskViewModel(
    private val repository: HouseholdRequirementRepository
) : ViewModel() {

    data class WeeklyTaskUiState(
        val id: String? = null,

        val taskName: String = "",
        val targetState: SlotState = SlotState.LIFE,

        val everyDay: Boolean = false,
        val daysOfWeek: Set<DayOfWeek> = emptySet(),

        val isTwoPersonTask: Boolean = false,
        val allowedPersonOption: AllowedPersonOption = AllowedPersonOption.EITHER,

        val startTime: LocalTime? = null,
        val durationMinutes: Int = 30,

        val isFlexible: Boolean = false,
        val flexMinutes: Int = 0
    )

    private val _uiState = MutableStateFlow(WeeklyTaskUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted = _saveCompleted.asSharedFlow()

    fun updateTaskName(name: String) =
        _uiState.update { it.copy(taskName = name) }

    fun updateTargetState(state: SlotState) =
        _uiState.update { it.copy(targetState = state) }

    fun updateTwoPerson(value: Boolean) =
        _uiState.update { it.copy(isTwoPersonTask = value) }

    fun updateAllowedPerson(option: AllowedPersonOption) =
        _uiState.update { it.copy(allowedPersonOption = option) }

    fun updateStartTime(time: LocalTime?) =
        _uiState.update { it.copy(startTime = time) }

    fun updateDuration(minutes: Int) =
        _uiState.update { it.copy(durationMinutes = minutes) }

    fun updateFlexible(value: Boolean) =
        _uiState.update { it.copy(isFlexible = value) }

    fun updateFlex(minutes: Int) =
        _uiState.update { it.copy(flexMinutes = minutes) }

    fun toggleEveryDay(value: Boolean) {

        _uiState.update {

            it.copy(
                everyDay = value,
                daysOfWeek =
                    if (value)
                        DayOfWeek.values().toSet()
                    else
                        emptySet()
            )
        }
    }

    fun toggleDay(day: DayOfWeek) {

        _uiState.update { state ->

            val newDays =
                if (day in state.daysOfWeek)
                    state.daysOfWeek - day
                else
                    state.daysOfWeek + day

            state.copy(
                daysOfWeek = newDays,
                everyDay = newDays.size == 7
            )
        }
    }

    fun onSave() {
        val input = _uiState.value
        val rule = convertToRule(input)

        viewModelScope.launch {

            repository.save(rule)

            _saveCompleted.emit(Unit)

            Log.d("WeeklySave", "Saved rule: $rule")
        }
    }

    fun convertToRule(input: WeeklyTaskUiState): HouseholdRequirementRule {

        val start = input.startTime ?: error("StartTime required")

        val requiredCount =
            if (input.isTwoPersonTask) 2 else 1

        val allowedPersons: Set<Person> =
            if (input.isTwoPersonTask) {
                Person.values().toSet()
            } else {
                when (input.allowedPersonOption) {
                    AllowedPersonOption.EITHER ->
                        Person.values().toSet()

                    AllowedPersonOption.FATHER_ONLY ->
                        setOf(Person.FATHER)

                    AllowedPersonOption.MOTHER_ONLY ->
                        setOf(Person.MOTHER)
                }
            }

        val flexSlots =
            if (!input.isFlexible) {
                FlexWindowParameters(0, 0)
            } else {
                val slot = input.flexMinutes / TimeAxis.stepMinutes
                FlexWindowParameters(slot, slot)
            }

        val endTime =
            start.plusMinutes(input.durationMinutes.toLong())

        return HouseholdRequirementRule(
            id = input.id ?: UUID.randomUUID().toString(),
            taskName = input.taskName.trim(),
            targetState = input.targetState,
            requiredCount = requiredCount,
            allowedPersons = allowedPersons,
            flexWindowSlots = flexSlots,
            date = null,
            daysOfWeek =
                if (input.everyDay)
                    DayOfWeek.values().toSet()
                else
                    input.daysOfWeek,
            timeRange = TimeRange(
                start = start,
                end = endTime
            )
        )
    }

    fun load(rule: HouseholdRequirementRule) {

        val start = rule.timeRange.start
        val end = rule.timeRange.end

        val duration =
            java.time.Duration.between(start, end).toMinutes().toInt()

        val isTwoPerson = rule.requiredCount >= 2

        val allowedOption =
            if (isTwoPerson) {
                AllowedPersonOption.EITHER
            } else {
                when (rule.allowedPersons) {
                    setOf(Person.FATHER) -> AllowedPersonOption.FATHER_ONLY
                    setOf(Person.MOTHER) -> AllowedPersonOption.MOTHER_ONLY
                    else -> AllowedPersonOption.EITHER
                }
            }

        val isFlexible =
            rule.flexWindowSlots.backward > 0 ||
                    rule.flexWindowSlots.forward > 0

        val flexMinutes =
            if (isFlexible) {
                rule.flexWindowSlots.backward * TimeAxis.stepMinutes
            } else 0

        val days = rule.daysOfWeek ?: emptySet()

        val isEveryDay = days.size == 7

        _uiState.value = WeeklyTaskUiState(
            id = rule.id,   // 重要

            taskName = rule.taskName,
            targetState = rule.targetState,

            everyDay = isEveryDay,
            daysOfWeek =
                if (isEveryDay) emptySet() else days,

            isTwoPersonTask = isTwoPerson,
            allowedPersonOption = allowedOption,

            startTime = start,
            durationMinutes = duration,

            isFlexible = isFlexible,
            flexMinutes = flexMinutes
        )
    }
}

