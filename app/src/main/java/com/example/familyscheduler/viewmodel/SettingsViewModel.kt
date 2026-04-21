package com.example.familyscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.utilities.SettingsUiState
import com.example.familyscheduler.ui.utilities.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = repository.settings
    fun updateMaxChildren(value: Int) {
        if (value < 1) return
        viewModelScope.launch {
            repository.update { it.copy(maxChildrenPerAdult = value) }
        }
    }
    fun updateMinutes(
        current: Int,
        delta: Int
    ): Int {
        val step = TimeAxis.stepMinutes
        val newValue = current + delta * step
        return newValue.coerceAtLeast(step)
    }
    fun updateBedtime(delta: Int) {
        viewModelScope.launch {
            repository.update {
                val newValue = updateMinutes(it.bedtimeMinutes, delta)
                it.copy(bedtimeMinutes = newValue)
            }
        }
    }
    fun updateDropOff(delta: Int) {
        viewModelScope.launch {
            repository.update {
                val newValue = updateMinutes(it.dropOffMinutes, delta)
                it.copy(dropOffMinutes = newValue)
            }
        }
    }
    fun updatePickup(delta: Int) {
        viewModelScope.launch {
            repository.update {
                val newValue = updateMinutes(it.pickupMinutes, delta)
                it.copy(pickupMinutes = newValue)
            }
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
