package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.data.repository.InMemoryDailyStateRepository
import com.example.familyscheduler.data.repository.InMemoryTemplateRepository
import com.example.familyscheduler.domain.evaluation.AvailabilityEngine
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class TimelineViewModel : ViewModel() {

    // 現在日付
    private val _currentDate =
        MutableStateFlow(LocalDate.now())

    val currentDate: StateFlow<LocalDate> =
        _currentDate

    // DailyState一覧（父・母）
    private val _dailyStates =
        MutableStateFlow<List<DailyState>>(emptyList())

    val dailyStates: StateFlow<List<DailyState>> =
        _dailyStates

    // Timeline表示用slots
    private val _slots =
        MutableStateFlow<List<TimeSlot>>(emptyList())

    val slots: StateFlow<List<TimeSlot>> =
        _slots

    private val _evaluations =
        MutableStateFlow<List<AvailabilityEvaluation>>(emptyList())

    val evaluations: StateFlow<List<AvailabilityEvaluation>> =
        _evaluations

    private val _householdRequirements =
        MutableStateFlow<List<HouseholdRequirement>>(emptyList())

    val householdRequirements: StateFlow<List<HouseholdRequirement>> =
        _householdRequirements

    var editingTemplateFor by mutableStateOf<Person?>(null)
        private set

    val templates by mutableStateOf<List<DailyTemplate>>(emptyList())

    // 初期化
    init {
        loadForDate(LocalDate.now())
    }

    // 日付ロード（中核）
    fun loadForDate(date: LocalDate) {

        viewModelScope.launch {

            _currentDate.value = date

            var states =
                InMemoryDailyStateRepository.get(date)

            if (states.isEmpty()) {

                val templates =
                    InMemoryTemplateRepository.getTemplates()

                val generated =
                    generateDailyStatesFromTemplates(
                        templates,
                        date
                    )

                generated.forEach {
                    InMemoryDailyStateRepository.save(it)
                }

                states =
                    InMemoryDailyStateRepository.get(date)

                Log.d("TimelineVM", "templates size = ${templates.size}")
                Log.d("TimelineVM", "states size = ${states.size}")
                Log.d("TimelineVM", "slots size = ${states.flatMap { it.slots }.size}")
            }

            _dailyStates.value = states

            _slots.value =
                states.flatMap { it.slots }

            recomputeAvailability()
        }
    }

    //編集（状態変更）★仮置き
    fun changeSlotState(
        index: Int,
        person: Person,
        newState: SlotState
    ) {
        _slots.value = _slots.value.map { slot ->
            if (slot.index == index && slot.person == person) {
                slot.copy(state = newState, flexWindow = 0, taskName = null)
            } else {
                slot
            }
        }

        recomputeAvailability()
    }

    // 前日
    fun moveToPreviousDay() {
        loadForDate(
            _currentDate.value.minusDays(1)
        )
    }

    // 翌日
    fun moveToNextDay() {
        loadForDate(
            _currentDate.value.plusDays(1)
        )
    }

    // Template → DailyState生成
    private fun generateDailyStatesFromTemplates(
        templates: List<DailyTemplate>,
        date: LocalDate
    ): List<DailyState> {

        return templates
            .filter { it.repeatRule.appliesTo(date) }
            .map { template ->

                val slots =
                    template.expandToSlots(date)

                DailyState(
                    person = template.person,
                    date = date,
                    templateName = template.name,
                    slots = slots
                )
            }
    }

    // UI用　（いらない？）
    fun slotsAt(index: Int): List<TimeSlot> {

        return _slots.value.filter {
            it.index == index
        }
    }

    fun templatesForPerson(
        person: Person
    ): List<DailyTemplate> {

        return InMemoryTemplateRepository
            .getTemplatesForPerson(person)
    }

    // Requirements
    fun setHouseholdRequirements(
        requirements: List<HouseholdRequirement>
    ) {
        _householdRequirements.value = requirements

        recomputeAvailability()
    }

    // 割り当て + 評価
    // UNASSIGNEDを探しChildCareSamples.allowedのSlotStateにする
    // →余ったUNASSIGNEDはFREEにする
    // →allowedを満たしているか全スロットを確認し、満たしてないRowに警告アイコンをだす）
    private fun recomputeAvailability() {

        val result =
            AvailabilityEngine.recompute(
                originalSlots = _slots.value,
                requirements = _householdRequirements.value
            )

        /* 下記はAvailabilityEngine内で行う
        if (slots.value.isEmpty()) return

        // requirementsが無い日はそのまま表示
        if (householdRequirements.isEmpty()) {
            _evaluations.value = emptyList()
            return
        }

        val workingSlots = _slots.value.toMutableList()

        assignHouseholdTasks(
            slots = workingSlots,
            requirements = householdRequirements
        )

        val evaluations = evaluateAvailability(
            slots = workingSlots,
            requirements = householdRequirements
        )

         */

        _slots.value = result.slots
        _evaluations.value = result.evaluations
    }

    fun onTemplateHeaderClick(person: Person) {
        editingTemplateFor = person
    }

    fun dismissTemplateSheet() {
        editingTemplateFor = null
    }

    fun applyTemplate(person: Person, template: DailyTemplate) {
        viewModelScope.launch {

            val slots = template.expandToSlots(currentDate.value)

            val state = DailyState(
                date = currentDate.value,
                person = person,
                templateName = template.name,
                slots = slots
            )

            InMemoryDailyStateRepository.save(state)

            loadForDate(currentDate.value)
            dismissTemplateSheet()
        }
    }
}
