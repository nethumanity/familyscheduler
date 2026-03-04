package com.example.familyscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.data.repository.InMemoryHouseholdRequirementRepository
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class OneTimeAppointmentViewModel(
    private val repository: InMemoryHouseholdRequirementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OneTimeAppointmentInput())
    val uiState: StateFlow<OneTimeAppointmentInput> = _uiState

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

    fun updateStartTime(time: LocalTime) =
        _uiState.update { it.copy(startTime = time) }

    fun updateDuration(minutes: Int) =
        _uiState.update { it.copy(durationMinutes = minutes) }

    fun updateFlexible(value: Boolean) =
        _uiState.update { it.copy(isFlexible = value) }

    fun updateFlex(minutes: Int) =
        _uiState.update { it.copy(flexMinutes = minutes) }

    fun isValid(): Boolean {
        val s = _uiState.value
        return s.taskName.isNotBlank() && s.startTime != null
    }

    fun onSave() {
        val input = _uiState.value
        val rule = convertToRule(input)
        val date = rule.date ?: return

        viewModelScope.launch {
            val current = repository.getByDate(date)
            val updated = current + rule
            repository.saveForDate(date, updated)
        }
    }

    private fun convertToRule(input: OneTimeAppointmentInput): HouseholdRequirementRule {

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
                val slot = input.flexMinutes / 30
                FlexWindowParameters(slot, slot)
            }

        val endTime =
            input.startTime!!.plusMinutes(input.durationMinutes.toLong())

        return HouseholdRequirementRule(
            taskName = input.taskName,
            targetState = input.targetState,
            requiredCount = requiredCount,
            allowedPersons = allowedPersons,
            flexWindowSlots = flexSlots,
            date = input.date,
            daysOfWeek = null,
            timeRange = TimeRange(
                start = input.startTime,
                end = endTime
            )
        )
    }

    data class OneTimeAppointmentInput(
        val date: LocalDate = LocalDate.now(),

        val taskName: String = "",
        val targetState: SlotState = SlotState.LIFE,

        val isTwoPersonTask: Boolean = false,
        val allowedPersonOption: AllowedPersonOption = AllowedPersonOption.EITHER,

        val startTime: LocalTime? = null,
        val durationMinutes: Int = 30,

        val isFlexible: Boolean = false,
        val flexMinutes: Int = 0
    )

    enum class AllowedPersonOption(label: String) {
        EITHER("どちらでも"),
        FATHER_ONLY("父のみ"),
        MOTHER_ONLY("母のみ")
    }
}