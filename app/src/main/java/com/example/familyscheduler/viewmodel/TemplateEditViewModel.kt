package com.example.familyscheduler.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class TemplateEditViewModel(
    private val templateRepository: TemplateRepository,
    person: Person
) : ViewModel() {

    data class TemplateEditUiState(

        val person: Person,

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

        val additionalSchedules: List<ScheduleTemplate> = emptyList(),

        val overlaps: List<Pair<ScheduleTemplate, ScheduleTemplate>> = emptyList()
    )

    private val _uiState =
        MutableStateFlow(TemplateEditUiState(person = person))
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
        _uiState.update { old ->
            val newState = old.copy(workStart = time)
            val all = buildAllSchedules(newState)
            newState.copy(overlaps = all.findOverlaps())
        }
    }

    fun updateWorkEnd(time: LocalTime) {
        _uiState.update { old ->
            val newState = old.copy(workEnd = time)
            val all = buildAllSchedules(newState)
            newState.copy(overlaps = all.findOverlaps())
        }
    }

    fun updateNoGoCommute(value: Boolean) =
        _uiState.update { it.copy(noGoCommute = value) }

    fun updateGoCommuteStart(time: LocalTime) {
        _uiState.update { old ->
            val newState = old.copy(goCommuteStart = time)
            val all = buildAllSchedules(newState)
            newState.copy(overlaps = all.findOverlaps())
        }
    }

    fun updateGoCommuteEnd(time: LocalTime) {
        _uiState.update { old ->
            val newState = old.copy(goCommuteEnd = time)
            val all = buildAllSchedules(newState)
            newState.copy(overlaps = all.findOverlaps())
        }
    }

    fun updateNoBackCommute(value: Boolean) =
        _uiState.update { it.copy(noBackCommute = value) }

    fun updateBackCommuteStart(time: LocalTime) {
        _uiState.update { old ->
            val newState = old.copy(backCommuteStart = time)
            val all = buildAllSchedules(newState)
            newState.copy(overlaps = all.findOverlaps())
        }
    }

    fun updateBackCommuteEnd(time: LocalTime) {
        _uiState.update { old ->
            val newState = old.copy(backCommuteEnd = time)
            val all = buildAllSchedules(newState)
            newState.copy(overlaps = all.findOverlaps())
        }
    }

    fun updateSleepStart(time: LocalTime) {
        _uiState.update { old ->
            val newState = old.copy(sleepStart = time)
            val all = buildAllSchedules(newState)
            newState.copy(overlaps = all.findOverlaps())
        }
    }

    fun updateSleepEnd(time: LocalTime) {
        _uiState.update { old ->
            val newState = old.copy(sleepEnd = time)
            val all = buildAllSchedules(newState)
            newState.copy(overlaps = all.findOverlaps())
        }
    }

    fun addAdditionalSchedule() {

        val newSchedule = ScheduleTemplate(

            type = ScheduleType.WORK,

            timeRange = TimeRange(
                LocalTime.of(14,0),
                LocalTime.of(15,0)
            )
        )

        _uiState.update { old ->

            val newState = old.copy(
                additionalSchedules = old.additionalSchedules + newSchedule
            )

            val all = buildAllSchedules(newState)

            newState.copy(overlaps = all.findOverlaps())

        }
    }

    fun removeAdditionalSchedule(index: Int) {

        _uiState.update { old ->

            val newList = old.additionalSchedules.toMutableList()
                .apply { removeAt(index) }

            val newState = old.copy(
                additionalSchedules = newList
            )

            val all = buildAllSchedules(newState)

            newState.copy(overlaps = all.findOverlaps())

        }
    }

    fun updateAdditionalType(
        index: Int,
        type: ScheduleType
    ) {

        _uiState.update { old ->

            val list = old.additionalSchedules.toMutableList()

            val updated =
                list[index].copy(type =type)

            list[index] = updated

            val newState = old.copy(
                additionalSchedules = list
            )

            val all = buildAllSchedules(newState)

            newState.copy(overlaps = all.findOverlaps())

        }
    }

    fun updateAdditionalStart(index: Int, time: LocalTime) {

        _uiState.update { old ->

            val list = old.additionalSchedules.toMutableList()

            val oldItem = list[index]

            list[index] =
                oldItem.copy(
                    timeRange = TimeRange(
                        time,
                        oldItem.timeRange.end
                    )
                )

            val newState = old.copy(
                additionalSchedules = list
            )

            val all = buildAllSchedules(newState)

            newState.copy(overlaps = all.findOverlaps())

        }
    }

    fun updateAdditionalEnd(index: Int, time: LocalTime) {

        _uiState.update { old ->

            val list = old.additionalSchedules.toMutableList()

            val oldItem = list[index]

            list[index] =
                oldItem.copy(
                    timeRange = TimeRange(
                        oldItem.timeRange.start,
                        time
                    )
                )

            val newState = old.copy(
                additionalSchedules = list
            )

            val all = buildAllSchedules(newState)

            newState.copy(overlaps = all.findOverlaps())
        }
    }

    private fun buildAllSchedules(ui: TemplateEditUiState): List<ScheduleTemplate> {
        return buildList {
            if (!ui.noWork) {
                add(ScheduleTemplate(ScheduleType.WORK, TimeRange(ui.workStart, ui.workEnd)))
            }
            if (!ui.noGoCommute) {
                add(ScheduleTemplate(ScheduleType.COMMUTE_GO, TimeRange(ui.goCommuteStart, ui.goCommuteEnd)))
            }
            if (!ui.noBackCommute) {
                add(ScheduleTemplate(ScheduleType.COMMUTE_BACK, TimeRange(ui.backCommuteStart, ui.backCommuteEnd)))
            }
            add(ScheduleTemplate(ScheduleType.SLEEP, TimeRange(ui.sleepStart, ui.sleepEnd)))
            addAll(ui.additionalSchedules)
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
    fun load(template: DailyTemplate) {

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

                is RepeatRule.Daily -> false to DayOfWeek.values().toSet()

                is RepeatRule.Weekly -> false to repeatRule.days.toSet()
            }

        // ---- SLEEPの復元----
        val (sleepStart, sleepEnd) = restoreSleepRange(sleepList)

        // ---- 現在のUIをベースにする（重要）----
        val current = _uiState.value

        // ---- UIにセット ----
        _uiState.value = current.copy(
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

            // SLEEP（必ずある前提なら!!でもOK）
            sleepStart = sleepStart,
            sleepEnd = sleepEnd,

            // 追加
            additionalSchedules = additional
        )
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
        val axisEnd = TimeAxis.all.last()

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
