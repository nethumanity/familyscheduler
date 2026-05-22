package com.example.familyscheduler.ui.state.repository

import com.example.familyscheduler.ui.state.SettingsUiState
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<SettingsUiState>
    suspend fun update(transform: (SettingsUiState) -> SettingsUiState)
}
