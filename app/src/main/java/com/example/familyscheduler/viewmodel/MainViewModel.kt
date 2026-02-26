package com.example.familyscheduler.viewmodel

import androidx.lifecycle.ViewModel
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.evaluation.AvailabilityState
import com.example.familyscheduler.domain.evaluation.BlockInfo
import com.example.familyscheduler.domain.proposal.FlexResolveProposal
import com.example.familyscheduler.domain.evaluation.MissingReason
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.RequirementType
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime


class MainViewModel(
) : ViewModel() {

    private val _slots =
        MutableStateFlow<List<TimeSlot>>(emptyList())

    val slots: StateFlow<List<TimeSlot>> =
        _slots

    private val _evaluations =
        MutableStateFlow<List<AvailabilityEvaluation>>(emptyList())

    val evaluations: StateFlow<List<AvailabilityEvaluation>> =
        _evaluations

    var householdRequirements: List<HouseholdRequirement> =
        emptyList()
        private set

    //外部API用（評価）
    fun availabilityStateAt(time: LocalTime): AvailabilityState {
        val index = TimeAxis.indexOf(time)
        return evaluations.value
            .find { it.index == index}
            ?.state
            ?: AvailabilityState.NONE
    }

    //警告項
    //警告ダイアログ制御
    private val _warningDialogIndex =
        MutableStateFlow<Int?>(null)

    val warningDialogIndex: StateFlow<Int?> =
        _warningDialogIndex

    fun onAvailabilityWarningClick(index: Int) {
        _warningDialogIndex.value = index
    }

    fun dismissWarningDialog() {
        _warningDialogIndex.value = null
    }

    fun applyFlexResolveProposal(proposal: FlexResolveProposal) {
        _slots.value = _slots.value.map { slot ->
            when {

                slot.index == proposal.candidateIndex &&
                        slot.person == proposal.person ->
                    slot.copy(state = proposal.targetState)


                slot.index == proposal.initialIndex &&
                        slot.person == proposal.person ->
                    slot.copy(state = SlotState.UNASSIGNED)

                else -> slot
            }
        }

        //recomputeAvailability()
        dismissWarningDialog()
    }

    companion object
}