package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.data.repository.InMemoryDailyStateRepository
import com.example.familyscheduler.data.repository.InMemoryTemplateRepository
import com.example.familyscheduler.domain.person.Person
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

        }

        //recomputeAvailability()
    }

    //編集（状態変更）仮置き
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

        //recomputeAvailability()
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
}
