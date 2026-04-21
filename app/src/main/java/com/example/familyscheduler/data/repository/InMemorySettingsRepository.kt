package com.example.familyscheduler.data.repository

import com.example.familyscheduler.ui.utilities.SettingsUiState
import com.example.familyscheduler.ui.utilities.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InMemorySettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(SettingsUiState())
    override val settings: StateFlow<SettingsUiState> = _settings
    override suspend fun update(transform: (SettingsUiState) -> SettingsUiState) {
        _settings.update { current -> transform(current) }
    }
}
