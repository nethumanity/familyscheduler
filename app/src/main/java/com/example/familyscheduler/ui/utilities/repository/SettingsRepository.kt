package com.example.familyscheduler.ui.utilities.repository

import com.example.familyscheduler.ui.utilities.SettingsUiState
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<SettingsUiState>
    suspend fun update(transform: (SettingsUiState) -> SettingsUiState)
}
