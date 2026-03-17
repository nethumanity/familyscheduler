package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.ScheduleType
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import com.example.familyscheduler.domain.time.TimeRange
import com.example.familyscheduler.domain.schedule.TemplateNormalizer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class TemplateEditViewModel(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    data class TemplateEditUiState(

        val person: Person = Person.FATHER,

        val templateName: String = "",

        val noWeeklyRule: Boolean = false,

        val selectedDays: Set<DayOfWeek> =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ),

        val noWork: Boolean = false,
        val workStart: LocalTime = LocalTime.of(9,0),
        val workEnd: LocalTime = LocalTime.of(18,0),

        val noGoCommute: Boolean = false,
        val goCommuteStart: LocalTime = LocalTime.of(8,0),
        val goCommuteEnd: LocalTime = LocalTime.of(9,0),

        val noBackCommute: Boolean = false,
        val backCommuteStart: LocalTime = LocalTime.of(18,0),
        val backCommuteEnd: LocalTime = LocalTime.of(19,0),

        val sleepStart: LocalTime = LocalTime.of(22,0),
        val sleepEnd: LocalTime = LocalTime.of(6,0),

        val additionalSchedules: List<ScheduleTemplate> = emptyList()
    )

    private val _uiState =
        MutableStateFlow(TemplateEditUiState())
    val uiState: StateFlow<TemplateEditUiState> =
        _uiState

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted = _saveCompleted.asSharedFlow()

    fun updateTemplateName(name: String) =
        _uiState.update { it.copy(templateName = name) }

    fun updatePerson(person: Person) {
        _uiState.update { it.copy(person = person) }
    }

    fun updateNoWeeklyRule(flag: Boolean) {
        _uiState.update { it.copy(noWeeklyRule = flag) }
    }

    fun toggleDay(day: DayOfWeek) {
        _uiState.update {
            val days =
                if (it.selectedDays.contains(day))
                    it.selectedDays - day
                else
                    it.selectedDays + day

            it.copy(selectedDays = days)
        }
    }

    fun updateNoWork(value: Boolean) =
        _uiState.update { it.copy(noWork = value) }

    fun updateWorkStart(time: LocalTime) {
        _uiState.update { it.copy(workStart = time) }
    }

    fun updateWorkEnd(time: LocalTime) {
        _uiState.update { it.copy(workEnd = time) }
    }

    fun updateNoGoCommute(value: Boolean) =
        _uiState.update { it.copy(noGoCommute = value) }

    fun updateGoCommuteStart(time: LocalTime) {
        _uiState.update { it.copy(goCommuteStart = time) }
    }

    fun updateGoCommuteEnd(time: LocalTime) {
        _uiState.update { it.copy(goCommuteEnd = time) }
    }

    fun updateNoBackCommute(value: Boolean) =
        _uiState.update { it.copy(noBackCommute = value) }

    fun updateBackCommuteStart(time: LocalTime) {
        _uiState.update { it.copy(backCommuteStart = time) }
    }

    fun updateBackCommuteEnd(time: LocalTime) {
        _uiState.update { it.copy(backCommuteEnd = time) }
    }

    fun updateSleepStart(time: LocalTime) {
        _uiState.update { it.copy(sleepStart = time) }
    }

    fun updateSleepEnd(time: LocalTime) {
        _uiState.update { it.copy(sleepEnd = time) }
    }

    fun addAdditionalSchedule() {

        val newSchedule = ScheduleTemplate(

            type = ScheduleType.WORK,

            timeRange = TimeRange(
                LocalTime.of(14,0),
                LocalTime.of(15,0)
            )
        )

        _uiState.update {

            it.copy(
                additionalSchedules =
                    it.additionalSchedules + newSchedule
            )
        }
    }

    fun removeAdditionalSchedule(index: Int) {

        _uiState.update {

            it.copy(
                additionalSchedules =
                    it.additionalSchedules.toMutableList()
                        .apply { removeAt(index) }
            )
        }
    }

    fun updateAdditionalType(
        index: Int,
        type: ScheduleType
    ) {

        _uiState.update {

            val list = it.additionalSchedules.toMutableList()

            val old = list[index]

            list[index] = old.copy(type = type)

            it.copy(additionalSchedules = list)
        }
    }

    fun updateAdditionalStart(index: Int, time: LocalTime) {

        _uiState.update {

            val list = it.additionalSchedules.toMutableList()

            val old = list[index]

            list[index] =
                old.copy(
                    timeRange = TimeRange(
                        time,
                        old.timeRange.end
                    )
                )

            it.copy(additionalSchedules = list)
        }
    }

    fun updateAdditionalEnd(index: Int, time: LocalTime) {

        _uiState.update {

            val list = it.additionalSchedules.toMutableList()

            val old = list[index]

            list[index] =
                old.copy(
                    timeRange = TimeRange(
                        time,
                        old.timeRange.end
                    )
                )

            it.copy(additionalSchedules = list)
        }
    }

    fun saveTemplate() {

        val ui = _uiState.value

        val repeatRule =
            when {
                !ui.noWeeklyRule && ui.selectedDays.size == 7 ->
                    RepeatRule.Daily

                !ui.noWeeklyRule ->
                    RepeatRule.Weekly(ui.selectedDays)

                else ->
                    RepeatRule.None
            }

        val rawList = buildList {

            if (!ui.noWork) {
                add(
                    ScheduleTemplate(
                        type = ScheduleType.WORK,
                        timeRange = TimeRange(ui.workStart, ui.workEnd)
                    )
                )
            }

            if (!ui.noGoCommute) {
                add(
                    ScheduleTemplate(
                        type = ScheduleType.COMMUTE_GO,
                        timeRange = TimeRange(ui.goCommuteStart, ui.goCommuteEnd)
                    )
                )
            }

            if (!ui.noBackCommute) {
                add(
                    ScheduleTemplate(
                        type = ScheduleType.COMMUTE_BACK,
                        timeRange = TimeRange(ui.backCommuteStart, ui.backCommuteEnd)
                    )
                )
            }

            add(
                ScheduleTemplate(
                    type = ScheduleType.SLEEP,
                    timeRange = TimeRange(ui.sleepStart, ui.sleepEnd)
                )
            )

            addAll(ui.additionalSchedules)
        }

        val schedules =
            TemplateNormalizer.normalize(rawList)

        val template =
            DailyTemplate(
                person = ui.person,
                name = ui.templateName,
                schedules = schedules,
                repeatRule = repeatRule
            )

        viewModelScope.launch {
            templateRepository.saveTemplate(template)
            Log.d("TemplateSave", "Saved template: $template")
            _saveCompleted.emit(Unit)
        }
    }
}
