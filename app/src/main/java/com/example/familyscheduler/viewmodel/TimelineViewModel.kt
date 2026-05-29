package com.example.familyscheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.evaluation.AvailabilityEngine
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.evaluation.FlexResolveProposal
import com.example.familyscheduler.domain.evaluation.ReasonEvaluation
import com.example.familyscheduler.domain.interaction.TimelineBlock
import com.example.familyscheduler.domain.interaction.TimelineBlockBuilder
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.RequirementBuilder
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.RequirementSemantics
import com.example.familyscheduler.domain.requirement.RequirementShiftOverride
import com.example.familyscheduler.domain.requirement.RequirementSource
import com.example.familyscheduler.domain.requirement.RequirementToggleOverride
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.requirement.repository.RequirementOverrideRepository
import com.example.familyscheduler.domain.routine.ChildCareEvent
import com.example.familyscheduler.domain.routine.ChildCareLabel
import com.example.familyscheduler.domain.routine.ChildCareRuleConverter
import com.example.familyscheduler.domain.routine.ChildRoutineBuilder
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.RoutineResolver
import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.domain.routine.repository.RoutineShiftOverrideRepository
import com.example.familyscheduler.domain.routine.repository.RoutineToggleOverrideRepository
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.event.UiEvent
import com.example.familyscheduler.ui.projection.CareStateUiModel
import com.example.familyscheduler.ui.projection.RequirementUiModel
import com.example.familyscheduler.ui.projection.WarningDialogKey
import com.example.familyscheduler.ui.projection.WarningUiModel
import com.example.familyscheduler.ui.projection.toCareStateUiModel
import com.example.familyscheduler.ui.projection.toRequirementUiModel
import com.example.familyscheduler.ui.projection.toWarningUiModel
import com.example.familyscheduler.ui.state.EditingTarget
import com.example.familyscheduler.ui.state.GuideState
import com.example.familyscheduler.ui.state.SettingsUiState
import com.example.familyscheduler.ui.state.repository.SettingsRepository
import com.example.familyscheduler.ui.utilities.RequirementUndoPayload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class TimelineViewModel(
    private val templateRepository: TemplateRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val householdRequirementRepository: HouseholdRequirementRepository,
    private val requirementOverrideRepository: RequirementOverrideRepository,
    private val childRoutineRepository: ChildRoutineRepository,
    private val routineToggleOverrideRepository: RoutineToggleOverrideRepository,
    private val routineShiftOverrideRepository: RoutineShiftOverrideRepository,
    private val routineResolver: RoutineResolver,
    private val childRoutineBuilder: ChildRoutineBuilder,
    private val childCareRuleConverter: ChildCareRuleConverter,
    private val requirementBuilder: RequirementBuilder,
    private val timelineBlockBuilder: TimelineBlockBuilder,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    data class WarningDialogState(
        val index: Int,
        val reasonIndex: Int,
        val evaluation: AvailabilityEvaluation
    )

    data class TimelineUiState(
        val date: LocalDate,
        val templates: List<DailyTemplate>,
        val dailyStates: List<DailyState>,
        val overrides: List<RequirementOverride>,
        val childRoutines: List<ChildRoutineInput>,
        val routineShiftOverrides: List<RoutineShiftOverride>,
        val childCareEvents: List<ChildCareEvent>,
        val slots: List<TimeSlot>,
        val slotsByIndex: Map<Int, List<TimeSlot>>,
        val slotsByPersonIndex: Map<Pair<Person, Int>, TimeSlot>,
        val evaluations: List<AvailabilityEvaluation>,
        val evaluationsByIndex: Map<Int, AvailabilityEvaluation>,
        val reasonsById: Map<String, List<ReasonEvaluation>>,
        val proposalsById: Map<String, List<FlexResolveProposal>>,
        val requirements: List<HouseholdRequirement>,
        val rules: List<HouseholdRequirementRule>,
        val ruleNameMap: Map<String, String>,
        val settings: SettingsUiState,
        val timelineBlocks: List<TimelineBlock>
    )

    data class DailyOverviewUiState(
        val date: LocalDate,
        val warningItems: List<WarningUiModel> = emptyList(),
        val careStateItems: List<CareStateUiModel> = emptyList(),
        val requirementItems: List<RequirementUiModel> = emptyList()
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
            routineShiftOverrides = emptyList(),
            childCareEvents = emptyList(),
            slots = emptyList(),
            slotsByIndex = emptyMap(),
            slotsByPersonIndex = emptyMap(),
            evaluations = emptyList(),
            evaluationsByIndex = emptyMap(),
            reasonsById = emptyMap(),
            proposalsById = emptyMap(),
            requirements = emptyList(),
            rules = emptyList(),
            ruleNameMap = emptyMap(),
            settings = SettingsUiState(),
            timelineBlocks = emptyList()
        )
    )
    val uiState: StateFlow<TimelineUiState> = _uiState

    private val _dailyOverviewUiState = MutableStateFlow(
        DailyOverviewUiState(
            date = LocalDate.now(),
            warningItems = emptyList(),
            careStateItems = emptyList(),
            requirementItems = emptyList()
        )
    )
    val dailyOverviewUiState: StateFlow<DailyOverviewUiState> =
        _dailyOverviewUiState

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
        // ② DailyState不足分の補完（副作用）
        // -------------------------------
        viewModelScope.launch {
            _currentDate.flatMapLatest { date ->
                combine(
                    templateRepository.getAllFlow(),
                    dailyStateRepository.getByDate(date)
                ) { templates, states ->

                    val existing = states

                    buildMissingStates(date, templates, existing)
                }

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

            val baseFlow = _currentDate.flatMapLatest { date ->
                combine(
                    templateRepository.getAllFlow(),
                    dailyStateRepository.getByDate(date)
                ) { templates, states ->

                    Triple(date, templates, states)
                }
            }

            val ruleFlow = _currentDate.flatMapLatest { date ->
                combine(
                    householdRequirementRepository.getByDate(date),
                    requirementOverrideRepository.getByDate(date)
                ) { rules, overrides ->
                    rules to overrides
                }
            }

            val childFlow = _currentDate.flatMapLatest { date ->
                combine(
                    childRoutineRepository.getAllFlow(),
                    routineToggleOverrideRepository.getByDate(date),
                    routineShiftOverrideRepository.getByDate(date)
                ) { routines, toggleOverrides, shiftOverrides ->
                    Triple(routines, toggleOverrides, shiftOverrides)
                }
            }

            val settingsFlow = settingsRepository.settings

            combine(
                baseFlow,
                ruleFlow,
                childFlow,
                settingsFlow
            ) { base, rule, child, settings ->

                val (date, templates, states) = base
                val (rules, overrides) = rule
                val (routines, toggleOverrides, shiftOverrides) = child

                computeUiState(
                    date = date,
                    templates = templates,
                    states = states,
                    rules = rules,
                    overrides = overrides,
                    routines = routines,
                    toggleOverrides = toggleOverrides,
                    shiftOverrides = shiftOverrides,
                    settings = settings
                )

            }.collect { state ->
                Log.d("Solver", buildSolverLog(state.evaluations))
                _uiState.value = state
                _dailyOverviewUiState.value = buildDailyOverviewUiState(state)
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
        states: List<DailyState>,
        rules: List<HouseholdRequirementRule>,
        overrides: List<RequirementOverride>,
        routines: List<ChildRoutineInput>,
        toggleOverrides: Map<Pair<String, LocalDate>, ChildTodayRoutine>,
        shiftOverrides: List<RoutineShiftOverride>,
        settings: SettingsUiState
    ): TimelineUiState {

        // ① DailyState展開
        val slots = states.flatMap { it.slots }

        // ② ChildRoutine → Rule
        val resolved =
            routineResolver.resolve(routines, date, toggleOverrides, shiftOverrides)

        val routineResult =
            childRoutineBuilder.build(date, resolved)

        val childRules =
            childCareRuleConverter.convert(routineResult.blocks, settings)

        val mergedRules =
            (rules.filter { it.source != RequirementSource.CHILD_ROUTINE }) + childRules

        // ③ Requirement生成
        val requirements =
            requirementBuilder.build(
                mergedRules,
                overrides
            )

        // ④ Solver
        val result =
            AvailabilityEngine.recompute(
                originalSlots = slots,
                requirements = requirements,
                overrides = overrides
            )

        val slotsByIndex = result.slots.groupBy { it.index }
        val slotsByPersonIndex = result.slots.associateBy { it.person to it.index }
        val evaluationsByIndex = result.evaluations.associateBy { it.index }
        val allReasons = result.evaluations.flatMap { it.reasons }
        val reasonsById = allReasons.groupBy { it.reason.sourceRuleId }
        val proposalsById = allReasons.flatMap { it.proposals }.groupBy { it.sourceRuleId }
        val ruleNameMap = mergedRules.associate { it.id to it.taskName }

        // ⑤ Interaction
        val timelineBlocks =
            timelineBlockBuilder.build(
                rules = mergedRules,
                requirements = requirements,
                slotsByIndex = slotsByIndex,
                requirementOverrides = overrides
            )

        return TimelineUiState(
            date = date,
            templates = templates,
            dailyStates = states,
            overrides = overrides,
            childRoutines = routines,
            routineShiftOverrides = shiftOverrides,
            childCareEvents = routineResult.events,
            slots = result.slots,
            slotsByIndex = slotsByIndex,
            slotsByPersonIndex = slotsByPersonIndex,
            evaluations = result.evaluations,
            evaluationsByIndex = evaluationsByIndex,
            reasonsById = reasonsById,
            proposalsById = proposalsById,
            requirements = requirements,
            rules = mergedRules,
            ruleNameMap = ruleNameMap,
            settings = settings,
            timelineBlocks = timelineBlocks
        )
    }

    private fun buildDailyOverviewUiState(
        state: TimelineUiState
    ): DailyOverviewUiState {

        val blocks =
            state.timelineBlocks.sortedBy { it.startIndex }

        // ------------------------------------------------
        // Warning
        // ------------------------------------------------
        val warningItems =
            blocks
                .filter { it.assignablePersons.size < it.requiredCount }
                .mapNotNull { block ->

                    val relatedEvaluation =
                        state.evaluationsByIndex[block.startIndex]
                            ?: return@mapNotNull null

                    val ruleId =
                        relatedEvaluation.reasons
                            .firstOrNull { it.reason.sourceRuleId in block.requirementIds }
                            ?.reason
                            ?.sourceRuleId
                            ?: return@mapNotNull null

                    val relatedProposals =
                        block.requirementIds.flatMap { id ->
                            state.proposalsById[id].orEmpty()
                        }

                    val name =
                        block.requirementIds
                            .firstNotNullOfOrNull { id ->
                                state.ruleNameMap[id]
                            }
                            .orEmpty()

                    val hasProposal =
                        relatedProposals.isNotEmpty()

                    block.toWarningUiModel(
                        ruleId = ruleId,
                        name = name,
                        hasProposal = hasProposal
                    )
                }

        // ------------------------------------------------
        // CareState
        // ------------------------------------------------
        val careStateItems =
            blocks
                .filter { it.semantics == RequirementSemantics.STATE }
                .map { block ->

                block.toCareStateUiModel()
            }

        // ------------------------------------------------
        // Requirement
        // ------------------------------------------------
        val requirementItems =
            blocks
                .filter { it.semantics != RequirementSemantics.STATE }
                .map { block ->

                    val name =
                        block.requirementIds
                            .firstNotNullOfOrNull { id ->
                                state.ruleNameMap[id]
                            }
                            .orEmpty()

                block.toRequirementUiModel(
                    name = name,
                    requirementShiftOverrides = state.overrides.filterIsInstance<RequirementShiftOverride>(),
                    routineShiftOverrides = state.routineShiftOverrides,
                    events = state.childCareEvents,
                )
            }

        return DailyOverviewUiState(
            date = state.date,
            warningItems = warningItems,
            careStateItems = careStateItems,
            requirementItems = requirementItems
        )
    }

    //編集機能（状態変更）
    fun toggleCareStateMode(item: CareStateUiModel) {

        viewModelScope.launch {

            val next = item.mode.next(
                false,
                item.soloApplicable,
                item.reverseAssignable
            )

            item.requirementIds.forEach { id ->

                requirementOverrideRepository.replace(
                    override = RequirementToggleOverride(
                        ruleId = id,
                        date = _currentDate.value,
                        mode = next
                    )
                )
            }
        }
    }

    fun toggleRequirementMode(item: RequirementUiModel) {

        viewModelScope.launch {

            val next = item.mode.next(
                item.cancelApplicable,
                item.soloApplicable,
                item.reverseAssignable
            )

            requirementOverrideRepository.replace(
                override = RequirementToggleOverride(
                    ruleId = item.requirementId,
                    date = _currentDate.value,
                    mode = next
                )
            )
        }
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

        val overrides = _uiState.value.overrides
            .filter { it.ruleId == ruleId }

        val payload = RequirementUndoPayload(
            requirement = rule,
            overrides = overrides
        )

        viewModelScope.launch {
            requirementOverrideRepository.deleteAllByRuleId(ruleId)
            householdRequirementRepository.delete(ruleId)

            // 編集中なら解除
            if (_editingTarget.value?.requirementId == ruleId) {
                _editingTarget.value = null
            }

            _events.emit(
                UiEvent.ShowUndoDelete(
                    onUndo = { undoDeleteRequirement(payload) }
                )
            )

            // UI通知（任意）
            //_deleteCompleted.emit(Unit)
        }
    }

    fun undoDeleteRequirement(payload: RequirementUndoPayload) {

        viewModelScope.launch {
            householdRequirementRepository.save(payload.requirement)
            payload.overrides.forEach {
                requirementOverrideRepository.replace(it)
            }
        }
    }

    fun changeSlotState(
        index: Int,
        person: Person,
        newState: SlotState
    ) {

        viewModelScope.launch {

            val states = _uiState.value.dailyStates.map { state ->

                if (state.person != person) return@map state

                val newSlots = state.slots.map { slot ->
                    if (slot.index == index) {
                        slot.copy(
                            state = newState,
                            taskIds = emptyList()
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
                    onUndo = { undoDeleteTemplate(template) }
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

    // 警告→提案→実行：編集機能
    fun openFirstWarningDialog(index: Int) {

        val evaluation =
            _uiState.value.evaluationsByIndex[index]
                ?: return

        _warningDialogState.value =
            WarningDialogState(
                index = index,
                reasonIndex = 0,
                evaluation = evaluation
            )
    }

    fun openWarningDialog(key: WarningDialogKey) {

        val evaluation = _uiState.value.evaluationsByIndex[key.index] ?: return

        val reasonIndex =
            evaluation.reasons.indexOfFirst {
                it.reason.sourceRuleId == key.ruleId
            }.takeIf { it >= 0 } ?: 0

        _warningDialogState.value =
            WarningDialogState(
                index = key.index,
                reasonIndex = reasonIndex,
                evaluation = evaluation
            )
    }

    fun dismissWarningDialog() {
        _warningDialogState.value = null
    }

    fun applyFlexResolveProposal(
        proposal: FlexResolveProposal
    ) {

        viewModelScope.launch {

            when (proposal.requirementSource) {

                RequirementSource.USER -> {
                    val existOverride =
                        _uiState.value.overrides
                            .filterIsInstance<RequirementShiftOverride>()
                            .firstOrNull {
                                it.ruleId == proposal.sourceRuleId &&
                                        it.date == _currentDate.value
                            }

                    val deltaSteps =
                        (proposal.candidateIndex - proposal.initialIndex) +
                                (existOverride?.deltaSteps ?: 0)

                    val before = existOverride
                    val new = RequirementShiftOverride(
                        ruleId = proposal.sourceRuleId,
                        date = _currentDate.value,
                        deltaSteps = deltaSteps
                    )

                    requirementOverrideRepository.replace(new)

                    _events.emit(
                        UiEvent.ShowUndoProposal(
                            onUndo = {
                                viewModelScope.launch {
                                    if (before != null) {
                                        requirementOverrideRepository.replace(before)
                                    } else {
                                        requirementOverrideRepository.delete(new)
                                    }
                                }
                            }
                        )
                    )
                }
                RequirementSource.NURSERY_DROP_OFF, RequirementSource.NURSERY_PICKUP -> {
                    val event =
                        _uiState.value.childCareEvents
                            .firstOrNull { it.eventId == proposal.sourceRuleId }
                            ?: return@launch

                    val label = when (proposal.requirementSource) {
                        RequirementSource.NURSERY_DROP_OFF -> ChildCareLabel.NURSERY_DROP_OFF
                        RequirementSource.NURSERY_PICKUP -> ChildCareLabel.NURSERY_PICKUP
                        else -> return@launch
                    }

                    val beforeMap = event.childIds.associateWith { childId ->
                        _uiState.value.routineShiftOverrides.firstOrNull {
                            it.childId == childId &&
                                    it.date == _currentDate.value &&
                                    it.eventType == label
                        }
                    }

                    val news = event.childIds.map { childId ->
                        RoutineShiftOverride(
                            childId = childId,
                            date = _currentDate.value,
                            eventType = label,
                            nurseryTime = TimeAxis.all[proposal.candidateIndex]
                        )
                    }

                    news.forEach { routineShiftOverrideRepository.replace(it) }

                    _events.emit(
                        UiEvent.ShowUndoProposal(
                            onUndo = {
                                viewModelScope.launch {
                                    news.forEach { routineShiftOverrideRepository.delete(it) }
                                    beforeMap.forEach { (_, before) ->
                                        before?.let { routineShiftOverrideRepository.replace(it) }
                                    }
                                }
                            }
                        )
                    )
                }
                else -> {}
            }
            dismissWarningDialog()
        }
    }

    fun clearProposal(ruleId: String) {

        val targetReq = _uiState.value.rules
            .firstOrNull { it.id == ruleId }
            ?: return

        when (targetReq.source) {

            RequirementSource.USER -> {
                viewModelScope.launch {
                    val override =
                        _uiState.value.overrides
                            .filterIsInstance<RequirementShiftOverride>()
                            .firstOrNull {
                                it.ruleId == ruleId &&
                                        it.date == _currentDate.value
                                        //&& it.isFromProposal
                            } ?: return@launch

                    requirementOverrideRepository.delete(override)
                }
            }
            RequirementSource.NURSERY_DROP_OFF, RequirementSource.NURSERY_PICKUP -> {
                viewModelScope.launch {
                    val event =
                        _uiState.value.childCareEvents
                            .firstOrNull { it.eventId == ruleId }
                            ?: return@launch

                    val overrides = event.childIds.mapNotNull { childId ->
                        _uiState.value.routineShiftOverrides.firstOrNull {
                            it.childId == childId &&
                                    it.date == _currentDate.value &&
                                    it.eventType == event.label
                                    //&& it.isFromProposal
                        }
                    }
                    overrides.forEach {
                        routineShiftOverrideRepository.delete(it)
                    }
                }
            }
            else -> return
        }
    }

    private fun buildSolverLog(evaluations: List<AvailabilityEvaluation>): String {
        return buildString {
            appendLine("=== SOLVER DETAIL ===")

            evaluations.forEach {
                appendLine("eval: $it")
            }

            appendLine("=================")
        }
    }
}
