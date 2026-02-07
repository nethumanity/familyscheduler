package com.example.familyscheduler

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.familyscheduler.domain.household.HouseholdRequirement
import com.example.familyscheduler.domain.logic.AvailabilityEvaluation
import com.example.familyscheduler.domain.model.AvailabilityState
import com.example.familyscheduler.domain.model.DailyState
import com.example.familyscheduler.domain.model.Person
import com.example.familyscheduler.domain.model.RequirementType
import com.example.familyscheduler.domain.model.SlotState
import com.example.familyscheduler.domain.model.TimeSlot
import com.example.familyscheduler.domain.household.HouseholdSamples
import com.example.familyscheduler.domain.logic.MissingReason
import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalDate
import java.time.LocalTime

class MainViewModel : ViewModel() {

    private val _dailyStates =
        mutableStateOf(
            mapOf(
                Person.FATHER to DailyState.OFFICE,
                Person.MOTHER to DailyState.REMOTE
            )
        )

    val dailyStates: State<Map<Person, DailyState>>
        get() = _dailyStates

    val persons = listOf(
        Person.FATHER,
        Person.MOTHER
    )

    var editingDailyStateFor by mutableStateOf<Person?>(null)
        private set

    fun onDailyStateLongPress(person: Person) {
        editingDailyStateFor = person
    }

    fun dismissDailyStateSheet() {
        editingDailyStateFor = null
    }

    val times: List<LocalTime> = TimeAxis.times

    private fun defaultFlexWindow(state: SlotState): Int =
        when (state) {
            SlotState.WORK -> 2
            SlotState.REST -> 2
            else -> 0
        }

    private fun buildSlotsFromDailyStates(): List<TimeSlot> {
        val date = LocalDate.now()
        return TimeAxis.times.flatMapIndexed { index, time ->
            persons.map { person ->
                val state = dailyStates.value[person]!!
                    .defaultSlotStateAt(index)

                TimeSlot(
                    date = date,
                    index = index,
                    person = person,
                    state = state,
                    flexWindow = defaultFlexWindow(state)
                )
            }
        }
    }

    private var slots by mutableStateOf(buildSlotsFromDailyStates())

    fun slotsAt(index: Int): List<TimeSlot> =
        slots.filter { it.index == index }

    fun findSlot(time: LocalTime, person: Person): TimeSlot? {
        val index = TimeAxis.indexOf(time)
        return slotsAt(index).find { it.person == person }
    }

    val householdRequirements: List<HouseholdRequirement> =
        HouseholdSamples.default()

    private val _evaluations =
        mutableStateOf<List<AvailabilityEvaluation>>(emptyList())
    val evaluations: State<List<AvailabilityEvaluation>> = _evaluations


    init {
        recomputeAvailability()
    }

    //外部API用
    fun availabilityStateAt(time: LocalTime): AvailabilityState {
        val index = TimeAxis.indexOf(time)
        return evaluations.value
            .find { it.index == index}
            ?.state
            ?: AvailabilityState.NONE
    }

    //割り当て項
    // UNASSIGNEDを探しChildCareSamples.allowedのSlotStateにする
    // →余ったUNASSIGNEDはFREEにする
    // →allowedを満たしているか全スロットを確認し、満たしてないRowに警告アイコンをだす）

    fun assignHouseholdTasks(
        slots: MutableList<TimeSlot>,
        requirements: List<HouseholdRequirement>
    ) {
        // FIX → FLEX
        val orderedReqs = requirements.sortedBy {
            if (it.type == RequirementType.FIX) 0 else 1
        }

        for (req in orderedReqs) {
            for (index in TimeAxis.indices) {
                if (!req.isRequiredAt(index)) continue

                val slotsAtIndex = slots.filter { it.index == index }

                val alreadySatisfied =
                    slotsAtIndex.count {
                        it.person in req.allowedPersons &&
                                it.state == req.targetState
                    }

                var remaining = req.requiredCount - alreadySatisfied
                if (remaining <= 0) continue

                val candidates =
                    slotsAtIndex
                        .filter {
                            it.person in req.allowedPersons &&
                                    it.state == SlotState.UNASSIGNED
                        }

                for (slot in candidates) {
                    if (remaining <= 0) break

                    val i = slots.indexOf(slot)
                    slots[i] = slot.copy(state = req.targetState)
                    remaining--
                }
            }
        }
    }

    //評価項
    fun evaluateAvailability(
        slots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>
    ): List<AvailabilityEvaluation> {

        return TimeAxis.indices.map { index ->

            val slotsAtIndex = slots.filter { it.index == index }
            val activeReqs = requirements.filter { it.isRequiredAt(index) }

            val reasons = mutableListOf<MissingReason>()

            activeReqs.forEach { req ->

                val satisfiedCount =
                    slotsAtIndex.count { slot ->
                        slot.person in req.allowedPersons &&
                                slot.state == req.targetState
                    }

                val required = req.requiredCount
                val assigned = satisfiedCount
                val missing = required - assigned

                if (missing > 0) {
                    reasons += MissingReason.NotEnoughPeople(
                        requirementName = req.name,
                        required = required,
                        assigned = assigned
                    )
                }

                if (assigned == 0 && required > 0 && req.allowedPersons.isEmpty()) {
                    reasons += MissingReason.NoAssignablePerson(
                        requirementName = req.name
                    )
                }
            }

            val totalRequired = activeReqs.sumOf { it.requiredCount }
            val totalAssigned =
                activeReqs.sumOf { req ->
                    slotsAtIndex.count { slot ->
                        slot.person in req.allowedPersons &&
                                slot.state == req.targetState
                    }.coerceAtMost(req.requiredCount)
                }

            val evaluation = AvailabilityEvaluation(
                index = index,
                requiredCount = activeReqs.sumOf { it.requiredCount },
                availableCount = slotsAtIndex.count { it.state == SlotState.FREE },
                hasFixRequirement = activeReqs.any { it.type == RequirementType.FIX },
                hasFlexRequirement = activeReqs.any { it.type == RequirementType.FLEX },
                missing = reasons.size,
                reasons = reasons
            )

            evaluation.copy(
                flexProposals = flexResolveProposalsAt(index)

            )
        }
    }

    private fun recomputeAvailability() {
        // 1. 現在の slots をコピー
        val workingSlots = slots.toMutableList()

        // 2. 自動割り当て
        assignHouseholdTasks(
            slots = workingSlots,
            requirements = householdRequirements
        )

        // 3. 評価
        val evaluations = evaluateAvailability(
            slots = workingSlots,
            requirements = householdRequirements
        )

        // 4. state 更新
        slots = workingSlots
        _evaluations.value = evaluations
    }

    /*旧バージョン
    private fun recomputeAvailability() {
        _evaluations.value =
            evaluateAvailability(
                slots = slots,
                householdRequirements = householdRequirements
            )
    }
     */

    //警告理由、Suggest項
    //警告ダイアログ制御
    var warningDialogIndex by mutableStateOf<Int?>(null)
        private set

    fun onAvailabilityWarningClick(index: Int) {
        warningDialogIndex = index
    }

    fun dismissWarningDialog() {
        warningDialogIndex = null
    }

    data class FlexResolveProposal(
        val requirementName: String,
        val person: Person?, //?を消して修正
        val candidateIndex: Int,
        val initialIndex: Int,
        val deltaMinutes: Int,
        val targetState: SlotState
    )

    //↓の関数をひとつずつ検証する
    private fun canAssign(
        person: Person,
        index: Int,
        requiredState: SlotState
    ): Boolean {
        val slot = slots.find { it.person == person && it.index == index }
            ?: return false

        return slot.state == SlotState.FREE || slot.state == SlotState.UNASSIGNED
    }

    fun generateFlexResolveProposalsForReason(
        index: Int,
        reason: MissingReason.NotEnoughPeople
    ): List<FlexResolveProposal> {

        val stepMinutes = 30
        val offsets = listOf(-1, 1)

        val requirement = householdRequirements
            .find { it.name == reason.requirementName }
            ?: return emptyList()

        return offsets.flatMap { offset ->
            val candidateIndex = index + offset
            if (candidateIndex !in TimeAxis.times.indices) return@flatMap emptyList()

            persons.mapNotNull { person ->

                // ① 移動元にその人が存在するか
                val candidateSlot = slots.find {
                    it.person == person && it.index == candidateIndex
                } ?: return@mapNotNull null

                // ② 移動元が動かせる状態か
                if (candidateSlot.state !in listOf(SlotState.FREE, SlotState.LIFE))
                    return@mapNotNull null

                // ③ 移動先で requirement を満たせるか
                if (!canAssign(person, index, SlotState.CHILDCARE))
                    return@mapNotNull null

                FlexResolveProposal(
                    requirementName = reason.requirementName,
                    person = person,
                    candidateIndex = candidateIndex,
                    initialIndex = index,
                    deltaMinutes = 30,
                    targetState = SlotState.CHILDCARE
                )
            }
        }
    }

    enum class SlotPriority(val weight: Int) {
        FIX_WORK(0),
        FIX_REST(0),
        FIX_FREE(0),
        FIX_CHILDCARE(0),
        FIX_LIFE(1),
        FLEX_WORK(2),
        FLEX_REST(3),
        FLEX_FREE(4),
        FLEX_CHILDCARE(5),
        FLEX_LIFE(6)
    }

    /*
    fun TimeSlot.priority(): SlotPriority =
        when {
            isFixed && state == SlotState.CHILDCARE -> SlotPriority.FIX_CHILDCARE
            isFixed && state == SlotState.LIFE -> SlotPriority.FIX_LIFE
            state == SlotState.WORK -> SlotPriority.WORK
            state == SlotState.REST -> SlotPriority.REST
            state == SlotState.FREE -> SlotPriority.FREE
            !isFixed && state == SlotState.CHILDCARE -> SlotPriority.FLEX_CHILDCARE
            !isFixed && state == SlotState.LIFE -> SlotPriority.FLEX_LIFE
            else -> SlotPriority.FREE
        }

    fun FlexResolveProposal.score(slots: List<TimeSlot>): Int {
        val candidateSlot = slots.find {
            it.person == person && it.index == candidateIndex
        } ?: return Int.MAX_VALUE

        val moveCost = candidateSlot.priority().weight
        val distanceCost = kotlin.math.abs(deltaMinutes) / 30

        return moveCost * 10 + distanceCost
    }
     */

    fun flexResolveProposalsAt(index: Int): List<FlexResolveProposal> {
        val evaluation = evaluations.value
            .find { it.index == index }
            ?: return emptyList()

        if (!evaluation.hasFlexRequirement) return emptyList()

        return evaluation.reasons
            .filterIsInstance<MissingReason.NotEnoughPeople>()
            .flatMap { reason ->
                generateFlexResolveProposalsForReason(index, reason)
            }
            //.sortedBy { it.score(slots) } //いずれ解除
    }

    /*
    fun generateFlexResolveProposals(
        evaluation: AvailabilityEvaluation
    ): List<FlexResolveProposal> {

        if (!evaluation.hasFlexRequirement) return emptyList()

        return evaluation.reasons
            .filterIsInstance<MissingReason.NotEnoughPeople>()
            .map { reason ->
                listOf(
                    FlexResolveProposal(
                        requirementName = reason.requirementName,
                        person = reason.person,
                        candidateIndex = targetIndex,
                        initialIndex = index,
                        deltaMinutes = 30,
                        targetState = SlotState.CHILDCARE
                    ),
                    FlexResolveProposal(
                        requirementName = reason.requirementName,
                        person = reason.person,
                        candidateIndex = targetIndex,
                        initialIndex = index,
                        deltaMinutes = 30,
                        targetState = SlotState.CHILDCARE
                    )
                )
            }
            .flatten()
    }

    fun generateFlexResolveProposals(
        index: Int,
        slots: List<TimeSlot>,
        requirements: List<HouseholdRequirement>
    ): List<FlexResolveProposal> {
        val proposals = mutableListOf<FlexResolveProposal>()

        val flexReqs = requirements.filter {
            it.type == RequirementType.FLEX &&
                    it.requiredCountAt(index) > 0
        }

        for (req in flexReqs) {
            val window = req.flexWindowSlots

            for (offset in -window..window) {
                if (offset == 0) continue
                val targetIndex = index + offset
                if (targetIndex !in TimeAxis.indices) continue

                val candidateSlots =
                    slots.filter { it.index == targetIndex }

                for (slot in candidateSlots) {
                    val allowedStates = req.allowed[slot.person] ?: continue

                    if (
                        slot.state in allowedStates &&
                        canMoveSlot(slot, from = targetIndex, to = index)
                    ) {
                        proposals += FlexResolveProposal(
                            requirementName = req.name,
                            person = slot.person,
                            fromIndex = targetIndex,
                            toIndex = index,
                            deltaMinutes = offset * TimeAxis.stepMinutes,
                            targetState = slot.state
                        )
                    }
                }
            }
        }
        return proposals
    }
    */

    //状態変更
    fun changeSlotState(
        index: Int,
        person: Person,
        newState: SlotState
    ) {
        slots = slots.map { slot ->
            if (slot.index == index && slot.person == person) {
                slot.copy(state = newState)
            } else {
                slot
            }
        }
        recomputeAvailability()
    }

    private fun reloadSlotsFor(
        person: Person,
        state: DailyState
    ) {
        slots = slots.map { slot ->
            if (slot.person == person) {
                slot.copy(
                    state = state.defaultSlotStateAt(slot.index)
                )
            } else {
                slot
            }
        }
        recomputeAvailability()
    }

    fun toggleDailyState(person: Person) {
        val newState =
            _dailyStates.value[person]?.next()
                ?: DailyState.OFFICE

        updateDailyState(person, newState)
    }

    fun updateDailyState(person: Person, state: DailyState) {
        _dailyStates.value =
            _dailyStates.value.toMutableMap().also {
                it[person] = state
            }

        reloadSlotsFor(person, state)
    }

    fun applyFlexResolveProposal(proposal: FlexResolveProposal) {
        slots = slots.map { slot ->
            when {
                // 元の場所を空ける
                slot.index == proposal.candidateIndex &&
                        slot.person == proposal.person ->
                    slot.copy(state = SlotState.UNASSIGNED)

                // 移動先に割り当て
                slot.index == proposal.initialIndex &&
                        slot.person == proposal.person ->
                    slot.copy(state = proposal.targetState)

                else -> slot
            }
        }

        recomputeAvailability()
        dismissWarningDialog()
    }

    /*
    fun createFlexMoveProposal(
        person: Person,
        fromIndex: Int,
        toIndex: Int,
        deltaMinutes: Int
    ): FlexMoveProposal? {

        val slot = slotsAt(fromIndex)
            .find { it.person == person }
            ?: return null

        // ★ flexWindowはSlotが持つ
        if (slot.flexWindow == 0) return null

        return FlexMoveProposal(
            person = person,
            state = slot.state,
            fromIndex = fromIndex,
            toIndex = toIndex,
            deltaMinutes = deltaMinutes
        )
    }
     */
}
