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
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class OneTimeTaskViewModel(
    private val repository: HouseholdRequirementRepository
) : ViewModel() {

    data class OneTimeTaskUiState(
        val id: String? = null,
        val date: LocalDate = LocalDate.now(),

        val taskName: String = "",
        val targetState: SlotState = SlotState.LIFE,

        val isTwoPersonTask: Boolean = false,
        val allowedPersonOption: AllowedPersonOption = AllowedPersonOption.EITHER,

        val startTime: LocalTime? = null,
        val durationSteps: Int = 1,

        val isFlexible: Boolean = false,
        val flexBackwardSteps: Int = 0,
        val flexForwardSteps: Int = 0
    )

    private val _uiState = MutableStateFlow(OneTimeTaskUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted = _saveCompleted.asSharedFlow()

    fun updateDate(date: LocalDate) =
        _uiState.update { it.copy(date = date) }

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

    fun updateDuration(steps: Int) =
        _uiState.update { it.copy(durationSteps = steps.coerceAtLeast(1)) }

    fun updateFlexible(value: Boolean) =
        _uiState.update { it.copy(isFlexible = value) }

    fun updateBackwardFlex(steps: Int) =
        _uiState.update { it.copy(flexBackwardSteps = steps.coerceAtLeast(0)) }

    fun updateForwardFlex(steps: Int) =
        _uiState.update { it.copy(flexForwardSteps = steps.coerceAtLeast(0)) }

    fun onSave() {
        val input = _uiState.value
        val rule = convertToRule(input)

        viewModelScope.launch {
            repository.save(rule)

            _saveCompleted.emit(Unit)

            Log.d("OneTimeSave", "Saved rule: $rule")
        }
    }

    private fun convertToRule(input: OneTimeTaskUiState): HouseholdRequirementRule {

        val start = input.startTime ?: error("StartTime required")

        val requiredCount =
            if (input.isTwoPersonTask) 2 else 1

        val allowedPersons: List<Person> =
            if (input.isTwoPersonTask) {
                Person.values().toList()
            } else {
                when (input.allowedPersonOption) {
                    AllowedPersonOption.EITHER ->
                        Person.values().toList()

                    AllowedPersonOption.FATHER_ONLY ->
                        listOf(Person.FATHER)

                    AllowedPersonOption.MOTHER_ONLY ->
                        listOf(Person.MOTHER)
                }
            }

        val stepMinutes = TimeAxis.stepMinutes

        val durationMinutes = input.durationSteps * stepMinutes

        val flexSlots =
            if (!input.isFlexible) {
                FlexWindowParameters(0, 0)
            } else {
                FlexWindowParameters(input.flexBackwardSteps, input.flexForwardSteps)
            }

        val endTime =
            start.plusMinutes(durationMinutes.toLong())

        return HouseholdRequirementRule(
            id = input.id ?: UUID.randomUUID().toString(),
            taskName = input.taskName.trim(),
            targetState = input.targetState,
            requiredCount = requiredCount,
            allowedPersons = allowedPersons,
            flexWindowSlots = flexSlots,
            date = input.date,
            daysOfWeek = null,
            timeRange = TimeRange(
                start = start,
                end = endTime
            )
        )
    }

    fun load(rule: HouseholdRequirementRule) {

        val start = rule.timeRange.start
        val end = rule.timeRange.end

        val isTwoPerson = rule.requiredCount >= 2

        val allowedOption =
            if (isTwoPerson) {
                AllowedPersonOption.EITHER
            } else {
                when (rule.allowedPersons) {
                    listOf(Person.FATHER) -> AllowedPersonOption.FATHER_ONLY
                    listOf(Person.MOTHER) -> AllowedPersonOption.MOTHER_ONLY
                    else -> AllowedPersonOption.EITHER
                }
            }

        val isFlexible =
            rule.flexWindowSlots.backward > 0 ||
                    rule.flexWindowSlots.forward > 0

        val stepMinutes = TimeAxis.stepMinutes

        val durationSteps = (java.time.Duration.between(start, end).toMinutes() / stepMinutes).toInt()

        val flexBackwardSteps =
            if (isFlexible) {
                rule.flexWindowSlots.backward
            } else 0

        val flexForwardSteps =
            if (isFlexible) {
                rule.flexWindowSlots.forward
            } else 0

        _uiState.value = OneTimeTaskUiState(
            id = rule.id,
            date = rule.date ?: LocalDate.now(),
            taskName = rule.taskName,
            targetState = rule.targetState,
            isTwoPersonTask = isTwoPerson,
            allowedPersonOption = allowedOption,
            startTime = start,
            durationSteps = durationSteps,
            isFlexible = isFlexible,
            flexBackwardSteps = flexBackwardSteps,
            flexForwardSteps = flexForwardSteps
        )
    }
}