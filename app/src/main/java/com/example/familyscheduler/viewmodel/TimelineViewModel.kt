package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.evaluation.AvailabilityEngine
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.routine.ChildCareRuleConverter
import com.example.familyscheduler.domain.routine.ChildRoutineBuilder
import com.example.familyscheduler.domain.routine.RoutineResolver
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.seeder.SampleDataSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class TimelineViewModel(
    private val templateRepository: TemplateRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val householdRequirementRepository: HouseholdRequirementRepository,
    private val childRoutineRepository: ChildRoutineRepository,
    private val routineResolver: RoutineResolver,
    private val childRoutineBuilder: ChildRoutineBuilder,
    private val childCareRuleConverter: ChildCareRuleConverter
) : ViewModel() {

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

    private val _templates =
        MutableStateFlow<List<DailyTemplate>>(emptyList())

    val templates: StateFlow<List<DailyTemplate>> = _templates

    private val ENABLE_SAMPLE_DATA = false   // falseでサンプル注入なし

    // 初期化
    init {
        viewModelScope.launch {

            if (ENABLE_SAMPLE_DATA &&
                templateRepository.getTemplates().isEmpty() &&
                householdRequirementRepository.getByDate(_currentDate.value).isEmpty() &&
                childRoutineRepository.getAll().isEmpty()) {

                SampleDataSeeder.seed(
                    templateRepository,
                    householdRequirementRepository,
                    childRoutineRepository
                )
            }

            loadForDate(LocalDate.now())    // 二重のlaunch
        }
    }

    // 日付ロード（中核）
    fun loadForDate(date: LocalDate) {

        viewModelScope.launch {

            if (_currentDate.value != date) {
                _currentDate.value = date
            }

            var states =
                dailyStateRepository.get(date)

            if (states.isEmpty()) {

                val templates =
                    templateRepository.getTemplates()

                val generated =
                    generateDailyStatesFromTemplates(
                        templates,
                        date
                    )

                generated.forEach {
                    dailyStateRepository.save(it)
                }

                states =
                    dailyStateRepository.get(date)

                Log.d("TimelineVM", "templates size = ${templates.size}")
                Log.d("TimelineVM", "states size = ${states.size}")
                Log.d("TimelineVM", "slots size = ${states.flatMap { it.slots }.size}")
            }

            updateSlots(states)

            buildChildRoutineRules(date)

            recomputeAvailability()
        }
    }

    //編集（状態変更）★仮置き
    fun changeSlotState(
        index: Int,
        person: Person,
        newState: SlotState
    ) {

        viewModelScope.launch {

            // 現状では_dailyStatesのスロットと_slotsのスロットは一致しないことに留意
            // proposalの実行処理の仕様によってはバグるかも
            // （代替案）TimeSlotにisUserLocked = falseを追加し、newSlotsはtrueにする
            val states = _dailyStates.value.map { state ->

                if (state.person != person) return@map state

                val newSlots = state.slots.map { slot ->
                    if (slot.index == index) {
                        slot.copy(
                            state = newState,
                            flexWindow = FlexWindowParameters(0,0),
                            taskName = null
                        )
                    } else slot
                }

                state.copy(slots = newSlots)
            }

            states.forEach {
                dailyStateRepository.save(it)
            }

            updateSlots(states)
            recomputeAvailability()
        }
    }

    private fun updateSlots(states: List<DailyState>) {
        _dailyStates.value = states
        _slots.value = states.flatMap { it.slots }
    }

    fun refreshAvailability() {
        viewModelScope.launch {
            recomputeAvailability()
        }
    }

    fun onChildRoutineChanged() {

        viewModelScope.launch {

            val date = _currentDate.value

            buildChildRoutineRules(date)

            recomputeAvailability()
        }
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

    // 将来用
    fun changeDate(date: LocalDate) {
        loadForDate(date)
    }

    fun reloadCurrentDate() {
        loadForDate(_currentDate.value)
    }

    // Template表示・選択 → DailyState生成
    fun showTemplateSheet(person: Person) {

        editingTemplateFor = person

        viewModelScope.launch {
            _templates.value =
                templateRepository.getTemplatesForPerson(person)
        }
    }

    private fun generateDailyStatesFromTemplates(
        templates: List<DailyTemplate>,
        date: LocalDate
    ): List<DailyState> {

        return templates
            .filter { it.repeatRule.appliesTo(date) }
            .map { template ->

                val slots =
                    template.expandToSlots()

                DailyState(
                    person = template.person,
                    date = date,
                    templateName = template.name,
                    slots = slots
                )
            }
    }

    private suspend fun buildChildRoutineRules(date: LocalDate) {

        // 私案は引数なしにして、val date = _currentDate.value

        val routines =
            childRoutineRepository.getAll()

        val resolved =
            routineResolver.resolve(routines, date)

        val blocks =
            childRoutineBuilder.build(date.dayOfWeek, resolved)

        val rules =
            childCareRuleConverter.convert(blocks)

        householdRequirementRepository.clearChildRoutineRules()

        householdRequirementRepository.saveAll(rules)
    }

    // 割り当て + 評価
    // UNASSIGNEDを探しChildCareSamples.allowedのSlotStateにする
    // →余ったUNASSIGNEDはFREEにする
    // →allowedを満たしているか全スロットを確認し、満たしてないRowに警告アイコンをだす）
    private suspend fun recomputeAvailability() {

        val date = _currentDate.value

        val rules =
            householdRequirementRepository.getByDate(date)

        // 理想はoriginalSlots = DailyState.slots
        val originalSlots = _dailyStates.value.flatMap { it.slots }

        Log.d("TimelineVM", "rules size = ${rules.size}")
        rules.forEach {
            Log.d("TimelineVM", "rule = $it")
        }

        val requirements =
            rules.map { it.toRequirement() }

        _householdRequirements.value = requirements

        val result =
            AvailabilityEngine.recompute(
                originalSlots = originalSlots, //_slots.value,
                requirements = requirements
            )

        _slots.value = result.slots.toList()
        _evaluations.value = result.evaluations //←Engineの中身を精査した後に必要性を判断
    }

    fun dismissTemplateSheet() {
        editingTemplateFor = null
    }

    fun applyTemplate(person: Person, template: DailyTemplate) {

        viewModelScope.launch {

            val slots = template.expandToSlots()

            val state = DailyState(
                date = currentDate.value,
                person = person,
                templateName = template.name,
                slots = slots
            )

            dailyStateRepository.save(state)

            loadForDate(currentDate.value)
            dismissTemplateSheet()
        }
    }
}
