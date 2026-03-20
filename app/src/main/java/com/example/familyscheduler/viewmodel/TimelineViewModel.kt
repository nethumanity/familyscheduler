package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.evaluation.AvailabilityEngine
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.evaluation.AvailabilityState
import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.routine.ChildCareRuleConverter
import com.example.familyscheduler.domain.routine.ChildRoutineBuilder
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.RoutineResolver
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.seeder.SampleDataSeeder
import com.example.familyscheduler.ui.utilities.GuideState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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

    data class WarningDialogState(
        val index: Int,
        val proposals: List<FlexResolveProposal>
    )

    private val _currentDate =
        MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> =
        _currentDate

    private val _templates =
        MutableStateFlow<List<DailyTemplate>>(emptyList())
    val templates: StateFlow<List<DailyTemplate>> = _templates

    private val _dailyStates =
        MutableStateFlow<List<DailyState>>(emptyList())
    val dailyStates: StateFlow<List<DailyState>> =
        _dailyStates

    private val _slots =
        MutableStateFlow<List<TimeSlot>>(emptyList())
    val slots: StateFlow<List<TimeSlot>> =
        _slots

    private val _childRoutines =
        MutableStateFlow<List<ChildRoutineInput>>(emptyList())
    val childRoutines: StateFlow<List<ChildRoutineInput>> =
        _childRoutines

    private val _householdRequirements =
        MutableStateFlow<List<HouseholdRequirement>>(emptyList())
    val householdRequirements: StateFlow<List<HouseholdRequirement>> =
        _householdRequirements

    private val _evaluations =
        MutableStateFlow<List<AvailabilityEvaluation>>(emptyList())
    val evaluations: StateFlow<List<AvailabilityEvaluation>> =
        _evaluations

    private val _warningDialogState =
        MutableStateFlow<WarningDialogState?>(null)
    val warningDialogState: StateFlow<WarningDialogState?> =
        _warningDialogState

    var editingTemplateFor by mutableStateOf<Person?>(null)
        private set

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

    // 描画機能：日付ロード（中核）
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

                _templates.value = templates

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

    private fun generateDailyStatesFromTemplates(
        templates: List<DailyTemplate>,
        date: LocalDate
    ): List<DailyState> {

        return templates
            .groupBy { it.person }
            .flatMap { (person, personTemplates) ->

                val resolved = personTemplates.resolveFor(date)

                val selected = resolved.take(1)

                selected.map { template ->

                    val slots = template.expandToSlots()

                    DailyState(
                        person = person,
                        date = date,
                        templateName = template.name,
                        slots = slots
                    )
                }
            }
    }

    fun List<DailyTemplate>.resolveFor(date: LocalDate): List<DailyTemplate> {

        return this
            .asSequence()
            .filter { it.repeatRule.appliesTo(date) }
            .sortedWith(
                compareByDescending<DailyTemplate> { it.repeatRule.specificity() }
                    .thenByDescending { it.repeatRule is RepeatRule.Weekly }
                    .thenByDescending { it.createdAt }
            )
            .toList()
    }

    private fun updateSlots(states: List<DailyState>) {
        _dailyStates.value = states
        _slots.value = states.flatMap { it.slots }
    }

    private suspend fun buildChildRoutineRules(date: LocalDate) {

        val routines =
            childRoutineRepository.getAll()

        _childRoutines.value = routines

        val resolved =
            routineResolver.resolve(routines, date)

        val blocks =
            childRoutineBuilder.build(date.dayOfWeek, resolved)

        val rules =
            childCareRuleConverter.convert(blocks)

        householdRequirementRepository.clearChildRoutineRules()

        householdRequirementRepository.saveAll(rules)
    }

    // UNASSIGNEDを探しtargetStateにする（割り当て）
    // →余ったUNASSIGNEDはFREEにする（補完）
    // →requiredCountを満たしているか全スロットを確認し、満たしてないRowに警告をだす（評価）
    private suspend fun recomputeAvailability() {

        val date = _currentDate.value

        val rules =
            householdRequirementRepository.getByDate(date)

        val overrides = emptyList<RequirementOverride>()    //今はスタブ
        //requirementOverrideRepository.getByDate(date) //実装予定

        val originalSlots = _dailyStates.value.flatMap { it.slots }

        Log.d("TimelineVM", "rules size = ${rules.size}")
        rules.forEach {
            Log.d("TimelineVM", "rule = $it")
        }

        val activeRules =
            applyOverrides(rules, overrides)

        val requirements =
            activeRules.map { it.toRequirement() }

        _householdRequirements.value = requirements

        val result =
            AvailabilityEngine.recompute(
                originalSlots = originalSlots,
                requirements = requirements
            )

        _slots.value = result.slots.toList()
        _evaluations.value = result.evaluations
    }

    fun applyOverrides(
        rules: List<HouseholdRequirementRule>,
        overrides: List<RequirementOverride>,
    ): List<HouseholdRequirementRule> {

        val disabledIds = overrides
            .filter { it.disabled }
            .map { it.ruleId }
            .toSet()

        return rules.filterNot { it.id in disabledIds }
    }

    //編集機能（状態変更）
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

    fun showTemplateSheet(person: Person) {

        editingTemplateFor = person

        viewModelScope.launch {
            _templates.value =
                templateRepository.getTemplatesForPerson(person)
        }
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

    //描画APIシリーズ
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

    // 前日（できればなくす）
    fun moveToPreviousDay() {
        loadForDate(
            _currentDate.value.minusDays(1)
        )
    }

    // 翌日（できればなくす）
    fun moveToNextDay() {
        loadForDate(
            _currentDate.value.plusDays(1)
        )
    }

    // 将来用（今後はこれを使う）
    fun changeDate(date: LocalDate) {
        loadForDate(date)
    }

    fun reloadCurrentDate() {
        loadForDate(_currentDate.value)
    }

    private val _guideState = MutableStateFlow(GuideState())
    val guideState: StateFlow<GuideState> = _guideState

    fun refreshGuideState() {
        val templates = _templates.value
        val childRoutines = _childRoutines.value

        _guideState.update { current ->
            GuideState(
                showFatherHint =
                    current.showFatherHint &&
                            templates.none { it.person == Person.FATHER },

                showMotherHint =
                    current.showMotherHint &&
                            templates.none { it.person == Person.MOTHER },

                showChildHint =
                    current.showChildHint &&
                            childRoutines.isEmpty()
            )
        }
    }

    // 警告→提案→実行：編集機能（今後の強化ポイント）
    fun onAvailabilityWarningClick(index: Int) {

        val evaluation = _evaluations.value.getOrNull(index)
            ?: return

        if (evaluation.state != AvailabilityState.WARN) return

        _warningDialogState.value =
            WarningDialogState(index, evaluation.flexProposals)
    }

    fun dismissWarningDialog() {
        _warningDialogState.value = null
    }

    fun applyFlexResolveProposal(proposal: FlexResolveProposal) {

        viewModelScope.launch {

            // 現状では_dailyStatesのスロットと_slotsのスロットは一致しないことに留意
            // proposalの実行処理の仕様によってはバグるかも
            // （代替案）TimeSlotにisUserLocked = falseを追加し、newSlotsはtrueにする
            val states = _dailyStates.value.map { state ->

                if (state.person != proposal.person) return@map state

                val newSlots = state.slots.map { slot ->
                    when {

                        slot.index == proposal.candidateIndex &&
                                slot.person == proposal.person ->
                            slot.copy(state = proposal.targetState)


                        slot.index == proposal.initialIndex &&
                                slot.person == proposal.person ->
                            slot.copy(state = SlotState.UNASSIGNED)     //initialはそのままでは？

                        else -> slot
                    }
                }

                state.copy(slots = newSlots)
            }

            states.forEach {
                dailyStateRepository.save(it)
            }

            updateSlots(states)
            recomputeAvailability()

            dismissWarningDialog()
        }
    }
}
