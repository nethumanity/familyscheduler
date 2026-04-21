package com.example.familyscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.ui.utilities.SettingsUiState
import com.example.familyscheduler.ui.utilities.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = repository.settings
    private fun clampStep(value: Int): Int {
        return value.coerceIn(1, 30)
    }
    fun updateMaxChildren(value: Int) {
        viewModelScope.launch {
            repository.update { it.copy(maxChildrenPerAdult = clampStep(value)) }
        }
    }
    fun updateBedtime(value: Int) {
        viewModelScope.launch {
            repository.update { it.copy(bedtimeSteps = clampStep(value)) }
        }
    }
    fun updateDropOff(value: Int) {
        viewModelScope.launch {
            repository.update { it.copy(dropOffSteps = clampStep(value)) }
        }
    }
    fun updatePickup(value: Int) {
        viewModelScope.launch {
            repository.update { it.copy(pickupSteps = clampStep(value)) }
        }
    }
    fun toggleLegend() {
        viewModelScope.launch {
            repository.update { it.copy(showLegend = !it.showLegend) }
        }
    }
    fun toggleTotal() {
        viewModelScope.launch {
            repository.update { it.copy(showTotal = !it.showTotal) }
        }
    }
}
