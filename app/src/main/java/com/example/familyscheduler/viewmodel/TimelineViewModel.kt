package com.example.familyscheduler.viewmodel

import android.util.Log
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
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.requirement.RequirementToggleOverride
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.requirement.repository.RequirementOverrideRepository
import com.example.familyscheduler.domain.routine.ChildCareRuleConverter
import com.example.familyscheduler.domain.routine.ChildRoutineBuilder
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.RoutineResolver
import com.example.familyscheduler.domain.routine.repository.ChildOverrideRepository
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
import com.example.familyscheduler.ui.utilities.EditingTarget
import com.example.familyscheduler.ui.utilities.GuideState
import com.example.familyscheduler.ui.utilities.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class TimelineViewModel(
    private val templateRepository: TemplateRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val householdRequirementRepository: HouseholdRequirementRepository,
    private val requirementOverrideRepository: RequirementOverrideRepository,
    private val childRoutineRepository: ChildRoutineRepository,
    private val childOverrideRepository: ChildOverrideRepository,
    private val routineResolver: RoutineResolver,
    private val childRoutineBuilder: ChildRoutineBuilder,
    private val childCareRuleConverter: ChildCareRuleConverter,
    private val requirementBuilder: RequirementBuilder
) : ViewModel() {

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    data class WarningDialogState(
        val index: Int,
        val reasonIndex: Int, //追加
        val proposals: List<FlexResolveProposal> //たぶん、削除可能
    )

    data class TimelineUiState(
        val date: LocalDate,
        val templates: List<DailyTemplate>,
        val dailyStates: List<DailyState>,
        val overrides: List<RequirementOverride>,
        val childRoutines: List<ChildRoutineInput>,
        val slots: List<TimeSlot>,
        val slotsByIndex: Map<Int, List<TimeSlot>>,
        val slotsByPersonIndex: Map<Pair<Person, Int>, TimeSlot>,
        val evaluations: List<AvailabilityEvaluation>,
        val evaluationsByIndex: Map<Int, AvailabilityEvaluation>,
        val requirements: List<HouseholdRequirement>,
        val rules: List<HouseholdRequirementRule>,
    )

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate

    private val _uiState = MutableStateFlow(
        TimelineUiState(
            date = LocalDate.now(),
            templates = emptyList(),
            dailyStates = emptyList(),
            overrides = emptyList(),
            childRoutines = emptyList(),
            slots = emptyList(),
            slotsByIndex = emptyMap(),
            slotsByPersonIndex = emptyMap(),
            evaluations = emptyList(),
            evaluationsByIndex = emptyMap(),
            requirements = emptyList(),
            rules = emptyList()
        )
    )
    val uiState: StateFlow<TimelineUiState> = _uiState

    private val ENABLE_SAMPLE_DATA = false   //falseでサンプル注入なし

    private val _warningDialogState =
        MutableStateFlow<WarningDialogState?>(null)
    val warningDialogState: StateFlow<WarningDialogState?> =
        _warningDialogState

    private val _guideState = MutableStateFlow(GuideState())
    val guideState: StateFlow<GuideState> = _guideState

    private val _editingTarget = MutableStateFlow<EditingTarget?>(null)
    val editingTarget: StateFlow<EditingTarget?> = _editingTarget

    init {
        // -------------------------------
        // ① ガイド表示の要否
        // -------------------------------
        viewModelScope.launch {
            uiState.collect { state ->
                _guideState.update { current ->
                    GuideState(
                        showFatherHint =
                            current.showFatherHint &&
                                    state.templates.none { it.person == Person.FATHER },

                        showMotherHint =
                            current.showMotherHint &&
                                    state.templates.none { it.person == Person.MOTHER },

                        showChildHint =
                            current.showChildHint &&
                                    state.childRoutines.isEmpty()
                    )
                }
            }
        }

        // -------------------------------
        // ① 初期データ投入（必要なら）
        // -------------------------------
        viewModelScope.launch {
            if (ENABLE_SAMPLE_DATA) {
                SampleDataSeeder.seed(
                    templateRepository,
                    householdRequirementRepository,
                    childRoutineRepository
                )
            }
        }
        // -------------------------------
        // ② DailyState不足分の補完（副作用）
        // -------------------------------
        viewModelScope.launch {
            combine(
                _currentDate,
                templateRepository.getAllFlow(),
                dailyStateRepository.getAllFlow()
            ) { date, templates, statesMap ->

                val existing =
                    statesMap
                        .filterKeys { it.first == date }
                        .values
                        .toList()

                buildMissingStates(date, templates, existing)

            }.collect { missingList ->

                missingList.forEach {
                    dailyStateRepository.save(it)
                }
            }
        }
        // -------------------------------
        // ③ UI計算（純関数）
        // -------------------------------
        viewModelScope.launch {

            val baseFlow = combine(
                _currentDate,
                templateRepository.getAllFlow(),
                dailyStateRepository.getAllFlow()
            ) { date: LocalDate,
                templates: List<DailyTemplate>,
                states: Map<Pair<LocalDate, Person>, DailyState> ->

                Triple(date, templates, states)
            }

            val ruleFlow = combine(
                householdRequirementRepository.getAllFlow(),
                requirementOverrideRepository.getAllFlow()
            ) { rules: List<HouseholdRequirementRule>,
                overrides: List<RequirementOverride> ->

                rules to overrides
            }

            val childFlow = combine(
                childRoutineRepository.getAllFlow(),
                childOverrideRepository.getAllFlow()
            ) { routines: List<ChildRoutineInput>,
                childOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine> ->

                routines to childOverrides
            }

            combine(
                baseFlow,
                ruleFlow,
                childFlow
            ) { base, rule, child ->

                val (date, templates, statesMap) = base
                val (rules, overrides) = rule
                val (routines, childOverrides) = child

                computeUiState(
                    date = date,
                    templates = templates,
                    statesMap = statesMap,
                    rules = rules,
                    overrides = overrides,
                    routines = routines,
                    childOverrides = childOverrides
                )

            }.collect { state ->
                Log.d("TimelineVM", buildUiLog(state))
                _uiState.value = state
            }

        }
    }

    private fun buildMissingStates(
        date: LocalDate,
        templates: List<DailyTemplate>,
        existing: List<DailyState>
    ): List<DailyState> {

        val existingPersons = existing.map { it.person }.toSet()
        val missing = Person.entries - existingPersons

        if (missing.isEmpty()) return emptyList()

        return missing.flatMap { person ->
            generateDailyStatesFromTemplates(
                person,
                templates.filter { it.person == person },
                date
            )
        }
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

    private fun List<DailyTemplate>.resolveFor(date: LocalDate): List<DailyTemplate> {

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

    private fun computeUiState(
        date: LocalDate,
        templates: List<DailyTemplate>,
        statesMap: Map<Pair<LocalDate, Person>, DailyState>,
        rules: List<HouseholdRequirementRule>,
        overrides: List<RequirementOverride>,
        routines: List<ChildRoutineInput>,
        childOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>
    ): TimelineUiState {

        // ① DailyState抽出
        val states =
            statesMap
                .filterKeys { it.first == date }
                .values
                .toList()

        val slots = states.flatMap { it.slots }

        // ② ChildRoutine → Rule
        val resolved =
            routineResolver.resolve(routines, date, childOverrides)

        val blocks =
            childRoutineBuilder.build(date.dayOfWeek, resolved)

        val childRules =
            childCareRuleConverter.convert(blocks, date)

        val mergedRules =
            (rules.filter { it.source != RequirementSource.CHILD_ROUTINE }
                .filter { it.isActiveOn(date) } // getByDateでRule取得するなら不要
                    ) + childRules

        // ③ Overrideフィルタ
        val overridesForDate =
            overrides.filter { it.date == date }

        // ④ Requirement生成
        val requirements =
            requirementBuilder.build(
                mergedRules,
                overridesForDate
            )

        // ⑤ Solver
        val result =
            AvailabilityEngine.recompute(
                originalSlots = slots,
                requirements = requirements,
                overrides = overridesForDate
            )

        val slotsByIndex = result.slots.groupBy { it.index }
        val slotsByPersonIndex = result.slots.associateBy { it.person to it.index }
        val evaluationsByIndex = result.evaluations.associateBy { it.index }

        return TimelineUiState(
            date = date,
            templates = templates,
            dailyStates = states,
            overrides = overridesForDate,
            childRoutines = routines,
            slots = result.slots,
            slotsByIndex = slotsByIndex,
            slotsByPersonIndex = slotsByPersonIndex,
            evaluations = result.evaluations,
            evaluationsByIndex = evaluationsByIndex,
            requirements = requirements,
            rules = mergedRules
        )
    }

    fun getAssignedPersons(ruleId: String): List<Person> {

        val req = _uiState.value.requirements
            .filterIsInstance<TimeRangeHouseholdRequirement>()
            .firstOrNull { it.sourceRuleId == ruleId }
            ?: return emptyList()

        val slotsAtIndex = _uiState.value.slotsByIndex[req.startIndex].orEmpty()

        return slotsAtIndex
            .filter {
                it.state == req.targetState &&
                        it.person in req.allowedPersons &&
                        req.name in it.taskName
            }
            .map { it.person }
    }

    //編集機能（状態変更）
    fun toggleRequirementMode(
        rule: HouseholdRequirementRule,
        req: TimeRangeHouseholdRequirement?
    ) {
        viewModelScope.launch {

            val current = resolveMode(rule.id, _uiState.value.overrides)

            val assignedPersons = getAssignedPersons(rule.id)

            val reversedPerson =
                if (rule.requiredCount == 1 && rule.allowedPersons.size == 2)
                    rule.allowedPersons.toList() - assignedPersons
                else emptyList()

            val reverseAssignable =
                if (req != null && reversedPerson.size == 1) // req != nullの代わりに↓を有効にしてもいい
                    canAssignInVm(
                        person = reversedPerson.single(),
                        index = req.startIndex, //?: TimeAxis.indexOf(rule.timeRange.start),
                        slotsByPersonIndex = _uiState.value.slotsByPersonIndex,
                        requiredState = rule.targetState
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
        }
    }

    fun resolveMode(
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

    fun canAssignInVm(
        person: Person,
        index: Int,
        slotsByPersonIndex: Map<Pair<Person, Int>, TimeSlot>,
        requiredState: SlotState
    ): Boolean {
        val slot = slotsByPersonIndex[person to index] ?: return false
        return slot.state.weight <= requiredState.weight
    }

    fun startEditRequirement(ruleId: String) {

        if (_editingTarget.value != null) return

        _editingTarget.value = EditingTarget(
            requirementId = ruleId
        )
    }

    fun deleteRequirement(ruleId: String) {

        val rule = _uiState.value.rules
            .find { it.id == ruleId }
            ?: return

        viewModelScope.launch {
            requirementOverrideRepository.deleteByRuleId(ruleId)
            householdRequirementRepository.delete(ruleId)

            // 編集中なら解除
            if (_editingTarget.value?.requirementId == ruleId) {
                _editingTarget.value = null
            }

            _events.emit(
                UiEvent.ShowUndoDelete(
                    message = "削除しました",
                    onUndo = {
                        undoDeleteRequirement(rule)
                    }
                )
            )

            // UI通知（任意）
            //_deleteCompleted.emit(Unit)
        }
    }

    fun undoDeleteRequirement(rule: HouseholdRequirementRule) {

        viewModelScope.launch {
            householdRequirementRepository.save(rule)
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
            val states = _uiState.value.dailyStates.map { state ->

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
        }
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
        }
    }

    fun startEditTemplate(templateId: String) {

        if (_editingTarget.value != null) return

        _editingTarget.value = EditingTarget(
            templateId = templateId
        )
    }

    fun deleteTemplate(templateId: String) {

        val template = uiState.value.templates
            .find { it.id == templateId }
            ?: return

        viewModelScope.launch {
            templateRepository.delete(templateId)

            // 編集中なら解除
            if (_editingTarget.value?.templateId == templateId) {
                _editingTarget.value = null
            }

            _events.emit(
                UiEvent.ShowUndoDelete(
                    message = "削除しました",
                    onUndo = {
                        undoDeleteTemplate(template)
                    }
                )
            )

            // UI通知（任意）
            //_deleteCompleted.emit(Unit)
        }
    }

    fun undoDeleteTemplate(template: DailyTemplate) {

        viewModelScope.launch {
            templateRepository.save(template)
        }
    }

    fun clearEditingTarget() {
        _editingTarget.value = null
    }

    // 日付変更
    fun changeDate(date: LocalDate) {
        _currentDate.value = date
    }

    // 警告→提案→実行：編集機能（今後の強化ポイント）
    fun onAvailabilityWarningClick(
        index: Int,
        reasonIndex: Int = 0
    ) {
        val evaluation = _uiState.value.evaluationsByIndex[index] ?: return
        //val evaluation = _uiState.value.evaluations.find { it.index == index } ?: return

        if (evaluation.state != AvailabilityState.WARN) return

        _warningDialogState.value =
            WarningDialogState(
                index = index,
                reasonIndex = reasonIndex,
                proposals = evaluation.flexProposals
            )
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
            val states = _uiState.value.dailyStates.map { state ->

                if (state.person !in proposal.persons) return@map state

                val newSlots = state.slots.map { slot ->
                    when {
                        // この部分は保険、重要なのはRequirementのOverride生成
                        // initial側はslotの変更なし（現状案）
                        // taskNameの引継ぎはSolverに任せる
                        slot.index == proposal.candidateIndex &&
                                slot.person in proposal.persons ->
                            slot.copy(
                                state = proposal.targetState //むしろいらないかも。block導入時に要否を決める
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
                _uiState.value.overrides
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

            dismissWarningDialog()
        }
    }

    private fun buildUiLog(state: TimelineUiState): String {
        return """
        ===== UI STATE =====
        date: ${state.date}
        templates: ${state.templates.size}
        dailyStates: ${state.dailyStates.size}
        overrides: ${state.overrides.size}
        childRoutines: ${state.childRoutines.size}
        
        slots: ${state.slots.size}
        evaluations: ${state.evaluations.size}
        
        requirements: ${state.requirements.size}
        rules: ${state.rules.size}
        ====================
    """.trimIndent()
    }

    private fun buildUiLogDetail(state: TimelineUiState): String {
        return buildString {
            appendLine("=== UI DETAIL ===")

            state.slots.forEach {
                appendLine("slot: $it")
            }

            state.evaluations.forEach {
                appendLine("eval: $it")
            }

            appendLine("=================")
        }
    }
}
