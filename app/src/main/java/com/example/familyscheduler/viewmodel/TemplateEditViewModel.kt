package com.example.familyscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.ScheduleType
import com.example.familyscheduler.domain.schedule.TemplateNormalizer
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeRange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

class TemplateEditViewModel(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    data class TemplateEditUiState(

        val id: String? = null,
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

        val additionalSchedules: List<AdditionalScheduleUi> = emptyList(),

        val overlaps: List<Pair<ScheduleTemplate, ScheduleTemplate>> = emptyList()
    )

    data class AdditionalScheduleUi(
        val type: ScheduleType,
        val start: LocalTime,
        val end: LocalTime
    )

    private val _uiState =
        MutableStateFlow(TemplateEditUiState())
    val uiState: StateFlow<TemplateEditUiState> =
        _uiState

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted = _saveCompleted.asSharedFlow()

    private fun TemplateEditUiState.recompute(): TemplateEditUiState {
        val all = toSchedules()
        return copy(overlaps = all.findOverlaps())
    }

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
        _uiState.update { it.copy(workStart = time).recompute() }
    }

    fun updateWorkEnd(time: LocalTime) {
        _uiState.update { it.copy(workEnd = time).recompute() }
    }

    fun updateNoGoCommute(value: Boolean) =
        _uiState.update { it.copy(noGoCommute = value) }

    fun updateGoCommuteStart(time: LocalTime) {
        _uiState.update { it.copy(goCommuteStart = time).recompute() }
    }

    fun updateGoCommuteEnd(time: LocalTime) {
        _uiState.update { it.copy(goCommuteEnd = time).recompute() }
    }

    fun updateNoBackCommute(value: Boolean) =
        _uiState.update { it.copy(noBackCommute = value) }

    fun updateBackCommuteStart(time: LocalTime) {
        _uiState.update { it.copy(backCommuteStart = time).recompute() }
    }

    fun updateBackCommuteEnd(time: LocalTime) {
        _uiState.update { it.copy(backCommuteEnd = time).recompute() }
    }

    fun updateSleepStart(time: LocalTime) {
        _uiState.update { it.copy(sleepStart = time).recompute() }
    }

    fun updateSleepEnd(time: LocalTime) {
        _uiState.update { it.copy(sleepEnd = time).recompute() }
    }

    fun addAdditionalSchedule() {

        val newSchedule = AdditionalScheduleUi(
            type = ScheduleType.ADDITIONAL_WORK,
            start = LocalTime.of(14,0),
            end = LocalTime.of(15,0)
        )

        _uiState.update {
            it.copy(
                additionalSchedules = it.additionalSchedules + newSchedule
            ).recompute()
        }
    }

    fun removeAdditionalSchedule(index: Int) {

        _uiState.update {
            it.copy(
                additionalSchedules =
                    it.additionalSchedules.toMutableList()
                        .apply { removeAt(index) }).recompute()
        }
    }

    fun updateAdditionalType(
        index: Int,
        type: ScheduleType
    ) {
        _uiState.update {
            it.copy(
                additionalSchedules =
                    it.additionalSchedules.toMutableList().apply {
                        this[index] = this[index].copy(type = type)
                    }
            ).recompute()
        }
    }

    fun updateAdditionalStart(index: Int, time: LocalTime) {

        _uiState.update {
            it.copy(
                additionalSchedules =
                    it.additionalSchedules.toMutableList().apply {
                        this[index] = this[index].copy(start = time)
                    }
            ).recompute()
        }
    }

    fun updateAdditionalEnd(index: Int, time: LocalTime) {

        _uiState.update {
            it.copy(
                additionalSchedules =
                    it.additionalSchedules.toMutableList().apply {
                        this[index] = this[index].copy(end = time)
                    }
            ).recompute()
        }
    }

    private fun TemplateEditUiState.toSchedules(): List<ScheduleTemplate> {
        return buildList {
            fun addIfValid(type: ScheduleType, start: LocalTime, end: LocalTime) {
                TimeRange.createOrNull(start, end)
                    ?.let { add(ScheduleTemplate(type, it)) }
            }
            if (!noWork) {
                addIfValid(ScheduleType.WORK, workStart, workEnd)
            }
            if (!noGoCommute) {
                addIfValid(ScheduleType.COMMUTE_GO, goCommuteStart, goCommuteEnd)
            }
            if (!noBackCommute) {
                addIfValid(ScheduleType.COMMUTE_BACK, backCommuteStart, backCommuteEnd)
            }
            addIfValid(ScheduleType.SLEEP, sleepStart, sleepEnd)
            addAll(
                additionalSchedules.mapNotNull {
                    TimeRange.createOrNull(it.start, it.end)
                        ?.let { range ->
                            ScheduleTemplate(it.type, range)
                        }
                }
            )
        }
    }

    fun List<ScheduleTemplate>.findOverlaps(): List<Pair<ScheduleTemplate, ScheduleTemplate>> {
        val result = mutableListOf<Pair<ScheduleTemplate, ScheduleTemplate>>()

        for (i in indices) {
            for (j in i + 1 until size) {
                val a = this[i]
                val b = this[j]

                if (a.timeRange.overlaps(b.timeRange)) {
                    result.add(a to b)
                }
            }
        }
        return result
    }

    fun isValid(state: TemplateEditUiState): Boolean {
        return state.templateName.isNotBlank()
                //&& state.overlaps.isEmpty()
    }

    fun saveTemplate() {

        val ui = _uiState.value

        val repeatRule =
            when {
                !ui.noWeeklyRule && ui.selectedDays.size == 7 ->
                    RepeatRule.Daily

                !ui.noWeeklyRule && ui.selectedDays.isNotEmpty() ->
                    RepeatRule.Weekly(ui.selectedDays)

                else ->
                    RepeatRule.None
            }

        val rawList = ui.toSchedules()

        val schedules =
            TemplateNormalizer.normalize(rawList)

        val template =
            DailyTemplate(
                id = ui.id ?: UUID.randomUUID().toString(),
                person = ui.person,
                name = ui.templateName.trim(),
                schedules = schedules,
                repeatRule = repeatRule
            )

        viewModelScope.launch {
            templateRepository.save(template)
            _saveCompleted.emit(Unit)
        }
    }

    fun resetForm() {
        _uiState.value = TemplateEditUiState()
    }

    fun load(templateId: String) {

        viewModelScope.launch {

            val template = templateRepository.getTemplateById(templateId).first() ?: return@launch

            val schedules = template.schedules

            // ---- 各Scheduleを取得 ----
            val work = schedules.find { it.type == ScheduleType.WORK }
            val go = schedules.find { it.type == ScheduleType.COMMUTE_GO }
            val back = schedules.find { it.type == ScheduleType.COMMUTE_BACK }
            val sleepList = schedules.filter { it.type == ScheduleType.SLEEP }

            val additional = schedules.filter {
                it.type !in ScheduleType.core
            }

            // ---- repeatRuleの復元 ----
            val (noWeeklyRule, selectedDays) =
                when (val repeatRule = template.repeatRule) {
                    is RepeatRule.None -> true to emptySet()

                    is RepeatRule.Daily -> false to DayOfWeek.entries.toSet()

                    is RepeatRule.Weekly -> false to repeatRule.days.toSet()
                }

            // ---- SLEEPの復元----
            val (sleepStart, sleepEnd) = restoreSleepRange(sleepList)

            // ---- 現在のUIをベースにする ----
            val current = _uiState.value

            // ---- UIにセット ----
            _uiState.value = current.copy(
                id = template.id,
                person = template.person,
                templateName = template.name,

                // repeat
                noWeeklyRule = noWeeklyRule,
                selectedDays = selectedDays,

                // WORK
                noWork = work == null,
                workStart = work?.timeRange?.start ?: current.workStart,
                workEnd = work?.timeRange?.end ?: current.workEnd,

                // GO COMMUTE
                noGoCommute = go == null,
                goCommuteStart = go?.timeRange?.start ?: current.goCommuteStart,
                goCommuteEnd = go?.timeRange?.end ?: current.goCommuteEnd,

                // BACK COMMUTE
                noBackCommute = back == null,
                backCommuteStart = back?.timeRange?.start ?: current.backCommuteStart,
                backCommuteEnd = back?.timeRange?.end ?: current.backCommuteEnd,

                // SLEEP
                sleepStart = sleepStart,
                sleepEnd = sleepEnd,

                // 追加
                additionalSchedules = additional.map {
                    AdditionalScheduleUi(
                        type = it.type,
                        start = it.timeRange.start,
                        end = it.timeRange.end
                    )
                }
            )
        }
    }

    private fun restoreSleepRange(
        list: List<ScheduleTemplate>
    ): Pair<LocalTime, LocalTime> {

        require(list.isNotEmpty()) { "SLEEPは最低1つ必要" }

        if (list.size == 1) {
            val r = list.first().timeRange
            return r.start to r.end
        }

        // 2つ（またはそれ以上）を想定
        val sorted = list.sortedBy { it.timeRange.start }

        val first = sorted.first()
        val last = sorted.last()

        val axisStart = TimeAxis.all.first()

        return when {
            // パターン1：日またぎ（典型）
            first.timeRange.start == axisStart -> {
                // [00:00 → X] と [Y → end]
                val start = last.timeRange.start
                val end = first.timeRange.end
                start to end
            }

            // パターン2：逆順（安全策）
            last.timeRange.end == LocalTime.MIDNIGHT -> {
                val start = first.timeRange.start
                val end = last.timeRange.end
                start to end
            }

            else -> {
                // フォールバック（通常連結）
                val start = sorted.minBy { it.timeRange.start }.timeRange.start
                val end = sorted.maxBy { it.timeRange.end }.timeRange.end
                start to end
            }
        }
    }
}
