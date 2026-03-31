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
import com.example.familyscheduler.domain.requirement.RequirementBuilder
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.RequirementShiftOverride
import com.example.familyscheduler.domain.requirement.RequirementToggleOverride
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.requirement.repository.RequirementOverrideRepository
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
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.seeder.SampleDataSeeder
import com.example.familyscheduler.ui.utilities.EditingTarget
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
    private val requirementOverrideRepository: RequirementOverrideRepository,
    private val childRoutineRepository: ChildRoutineRepository,
    private val routineResolver: RoutineResolver,
    private val childRoutineBuilder: ChildRoutineBuilder,
    private val childCareRuleConverter: ChildCareRuleConverter,
    private val requirementBuilder: RequirementBuilder
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

    private val _householdRequirementRules =
        MutableStateFlow<List<HouseholdRequirementRule>>(emptyList())
    val householdRequirementRules: StateFlow<List<HouseholdRequirementRule>> =
        _householdRequirementRules

    private val _householdRequirements =
        MutableStateFlow<List<HouseholdRequirement>>(emptyList())
    val householdRequirements: StateFlow<List<HouseholdRequirement>> =
        _householdRequirements

    private val _overrides =
        MutableStateFlow<List<RequirementOverride>>(emptyList())

    val overrides: StateFlow<List<RequirementOverride>> =
        _overrides

    private val _evaluations =
        MutableStateFlow<List<AvailabilityEvaluation>>(emptyList())
    val evaluations: StateFlow<List<AvailabilityEvaluation>> =
        _evaluations

    private val _warningDialogState =
        MutableStateFlow<WarningDialogState?>(null)
    val warningDialogState: StateFlow<WarningDialogState?> =
        _warningDialogState

    private val _guideState = MutableStateFlow(GuideState())
    val guideState: StateFlow<GuideState> = _guideState

    var editingTemplateFor by mutableStateOf<Person?>(null)
        private set

    private val _editingTarget = MutableStateFlow<EditingTarget?>(null)
    val editingTarget: StateFlow<EditingTarget?> = _editingTarget

    private val ENABLE_SAMPLE_DATA = true   // falseでサンプル注入なし

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

                _guideState.value = GuideState(false, false, false)
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

            val states = ensureDailyStates(date)

            updateSlots(states)

            buildChildRoutineRules(date)

            recomputeAvailability()
        }
    }

    suspend fun ensureDailyStates(date: LocalDate): List<DailyState> {

        val states = dailyStateRepository.get(date)

        val existing = states.map { it.person }.toSet()
        val missing = Person.entries - existing

        if (missing.isEmpty()) return states

        val templates = templateRepository.getTemplates()

        _templates.value = templates

        val generated = missing.flatMap { person ->

            generateDailyStatesFromTemplates(
                person,
                templates.filter { it.person == person},
                date
            )
        }

        generated.forEach { dailyStateRepository.save(it) }

        return dailyStateRepository.get(date)
    }

    private fun generateDailyStatesFromTemplates(
        person: Person,
        templates: List<DailyTemplate>,
        date: LocalDate
    ): List<DailyState> {

        val resolved = templates.resolveFor(date)

        val selected = resolved.take(1)

        return selected.map { template ->

            val slots = template.expandToSlots()

            DailyState(
                person = person,
                date = date,
                templateName = template.name,
                slots = slots
            )
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

        _householdRequirementRules.value = rules

        val overrides = requirementOverrideRepository.getOverrides(date)

        val assignedPersonsMap: Map<String, List<Person>> =
            _householdRequirementRules.value
                .associate { rule ->
                    val persons = getAssignedPersons(rule.id)

                    rule.id to persons
                }

        val originalSlots = _dailyStates.value.flatMap { it.slots }

        val requirements =
            requirementBuilder.build(rules, overrides, assignedPersonsMap)

        _householdRequirements.value = requirements

        val result =
            AvailabilityEngine.recompute(
                originalSlots = originalSlots,
                requirements = requirements
            )

        _slots.value = result.slots.toList()
        _evaluations.value = result.evaluations

        Log.d("TimelineVM", "rules size = ${rules.size}")
        Log.d("TimelineVM", "rules = ${rules}")

        Log.d("TimelineVM", "reqs size = ${requirements.size}")
        Log.d("TimelineVM", "reqs = ${requirements}")

        Log.d("TimelineVM", "overrides size = ${overrides.size}")
        Log.d("TimelineVM", "overrides = ${overrides}")

        result.slots.forEach { slot ->
            Log.d("TimelineVM", "slot = ${slot}")
        }
        result.evaluations.forEach { evaluation ->
            Log.d("TimelineVM", "evaluation = ${evaluation}")
        }
    }

    fun getAssignedPersons(ruleId: String): List<Person> {

        val req = _householdRequirements.value
            .filterIsInstance<TimeRangeHouseholdRequirement>()
            .firstOrNull { it.sourceRuleId == ruleId }
            ?: return emptyList()

        return _slots.value
            .filter {
                it.index == req.startIndex &&
                        it.state == req.targetState &&
                        it.person in req.allowedPersons &&
                        req.name in it.taskName
            }
            .mapNotNull { it.person }
    }

    //編集機能（状態変更）
    fun toggleRequirementMode(
        rule: HouseholdRequirementRule
    ) {
        viewModelScope.launch {

            val current = resolveMode(rule.id, _overrides.value)

            val assignedPersons = getAssignedPersons(rule.id)

            val reversedPerson =
                if (rule.requiredCount == 1 && rule.allowedPersons.size == 2)
                    rule.allowedPersons.toList() - assignedPersons
                else emptyList()

            val reverseAssignable =
                if (reversedPerson.size == 1)
                    AvailabilityEngine.canAssign(
                        reversedPerson.single(),
                        TimeAxis.indexOf(rule.timeRange.start),
                        _slots.value,
                        rule.targetState
                    )
                else false

            val next = current.next(reverseAssignable)

            Log.d("override", "current=$current next=$next")

            requirementOverrideRepository.saveOverride(
                override = RequirementToggleOverride(
                    ruleId = rule.id,
                    date = _currentDate.value,
                    mode = next
                )
            )

            _overrides.value = requirementOverrideRepository.getOverrides(_currentDate.value)
        }
    }

    fun resolveMode(
        //req: HouseholdRequirement,
        //date: LocalDate,
        id: String,
        overrides: List<RequirementOverride>
    ): RequirementModeToday {

        overrides
            .filterIsInstance<RequirementToggleOverride>()
            .firstOrNull { it.ruleId == id && it.date == _currentDate.value }
            ?.let {
                return it.mode
            }

        return RequirementModeToday.AUTO
    }

    fun startEditRequirement(ruleId: String) {

        if (_editingTarget.value != null) return

        _editingTarget.value = EditingTarget(
            requirementId = ruleId
        )
    }

    fun deleteRequirement(ruleId: String) {
        viewModelScope.launch {
            requirementOverrideRepository.deleteByRuleId(ruleId)
            householdRequirementRepository.delete(ruleId)

            // 編集中なら解除
            if (_editingTarget.value?.requirementId == ruleId) {
                _editingTarget.value = null
            }
            refreshAvailability()

            // UI通知（任意）
            //_deleteCompleted.emit(Unit)
        }
    }

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
                            taskName = emptyList()
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

    fun startEditTemplate(templateId: String) {

        if (_editingTarget.value != null) return

        _editingTarget.value = EditingTarget(
            templateId = templateId
        )
    }

    fun deleteTemplate(templateId: String, person:Person) {
        viewModelScope.launch {
            templateRepository.delete(templateId)

            // 編集中なら解除
            if (_editingTarget.value?.templateId == templateId) {
                _editingTarget.value = null
            }

            _templates.value = templateRepository.getTemplatesForPerson(person)
            loadForDate(currentDate.value)

            // UI通知（任意）
            //_deleteCompleted.emit(Unit)
        }
    }

    fun clearEditingTarget() {
        _editingTarget.value = null
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

        val evaluation = _evaluations.value.find { it.index == index }
            ?: return

        if (evaluation.state != AvailabilityState.WARN) return

        _warningDialogState.value =
            WarningDialogState(index, evaluation.flexProposals)
    }

    fun dismissWarningDialog() {
        _warningDialogState.value = null
    }

    fun applyFlexResolveProposal(
        proposal: FlexResolveProposal
    ) {

        viewModelScope.launch {

            // 現状では_dailyStatesのスロットと_slotsのスロットは一致しないことに留意
            // proposalの実行処理の仕様によってはバグるかも
            // （代替案）TimeSlotにisUserLocked = falseを追加し、newSlotsはtrueにする
            val states = _dailyStates.value.map { state ->

                if (state.person !in proposal.persons) return@map state

                val newSlots = state.slots.map { slot ->
                    when {
                        // この部分は保険、重要なのはRequirementのOverride生成
                        // initial側はslotの変更なし（現状案）
                        // taskNameの引継ぎはSolverに任せる
                        slot.index == proposal.candidateIndex &&
                                slot.person in proposal.persons ->
                            slot.copy(
                                state = proposal.targetState
                            )

                        else -> slot
                    }
                }

                state.copy(slots = newSlots)
            }

            states.forEach {
                dailyStateRepository.save(it)
            }

            val existOverride =
                _overrides.value
                    .filterIsInstance<RequirementShiftOverride>()
                    .firstOrNull {it.ruleId == proposal.sourceRuleId}

            val deltaSteps =
                if(existOverride != null) {
                    proposal.candidateIndex - proposal.initialIndex + existOverride.deltaSteps
                } else {proposal.candidateIndex - proposal.initialIndex}

            requirementOverrideRepository.saveOverride(
                override = RequirementShiftOverride(
                    ruleId = proposal.sourceRuleId,
                    date = _currentDate.value,
                    deltaSteps = deltaSteps
                )
            )

            _overrides.value = requirementOverrideRepository.getOverrides(_currentDate.value)

            updateSlots(states)
            recomputeAvailability()

            dismissWarningDialog()
        }
    }
}
