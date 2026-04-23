package com.example.familyscheduler.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.familyscheduler.data.local.datastore.SettingsKeys
import com.example.familyscheduler.data.local.datastore.settingsDataStore
import com.example.familyscheduler.ui.utilities.SettingsUiState
import com.example.familyscheduler.ui.utilities.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DataStoreSettingsRepository(
    private val context: Context
) : SettingsRepository {

    override val settings: StateFlow<SettingsUiState> =
        context.settingsDataStore.data
            .map { prefs ->

                SettingsUiState(
                    maxChildrenPerAdult =
                        prefs[SettingsKeys.MAX_CHILDREN_PER_ADULT] ?: 2,

                    bedtimeSteps =
                        prefs[SettingsKeys.BEDTIME_STEPS] ?: 1,

                    dropOffSteps =
                        prefs[SettingsKeys.DROP_OFF_STEPS] ?: 1,

                    pickupSteps =
                        prefs[SettingsKeys.PICKUP_STEPS] ?: 1,

                    showLegend =
                        prefs[SettingsKeys.SHOW_LEGEND] ?: false,

                    showTotal =
                        prefs[SettingsKeys.SHOW_TOTAL] ?: false,

                    timelineStartIndex =
                        prefs[SettingsKeys.TIMELINE_START_INDEX] ?: 10,

                    timelineEndIndex =
                        prefs[SettingsKeys.TIMELINE_END_INDEX] ?: 47,

                    timelineStepMinutes =
                        prefs[SettingsKeys.TIMELINE_STEP_MINUTES] ?: 30
                )
            }
            .stateIn(
                scope = CoroutineScope(Dispatchers.IO),
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SettingsUiState()
            )

    override suspend fun update(
        transform: (SettingsUiState) -> SettingsUiState
    ) {
        context.settingsDataStore.edit { prefs ->

            val current = SettingsUiState(
                maxChildrenPerAdult = prefs[SettingsKeys.MAX_CHILDREN_PER_ADULT] ?: 2,
                bedtimeSteps = prefs[SettingsKeys.BEDTIME_STEPS] ?: 1,
                dropOffSteps = prefs[SettingsKeys.DROP_OFF_STEPS] ?: 1,
                pickupSteps = prefs[SettingsKeys.PICKUP_STEPS] ?: 1,
                showLegend = prefs[SettingsKeys.SHOW_LEGEND] ?: false,
                showTotal = prefs[SettingsKeys.SHOW_TOTAL] ?: false,
                timelineStartIndex = prefs[SettingsKeys.TIMELINE_START_INDEX] ?: 10,
                timelineEndIndex = prefs[SettingsKeys.TIMELINE_END_INDEX] ?: 47,
                timelineStepMinutes = prefs[SettingsKeys.TIMELINE_STEP_MINUTES] ?: 30
            )

            val updated = transform(current)

            prefs[SettingsKeys.MAX_CHILDREN_PER_ADULT] = updated.maxChildrenPerAdult
            prefs[SettingsKeys.BEDTIME_STEPS] = updated.bedtimeSteps
            prefs[SettingsKeys.DROP_OFF_STEPS] = updated.dropOffSteps
            prefs[SettingsKeys.PICKUP_STEPS] = updated.pickupSteps
            prefs[SettingsKeys.SHOW_LEGEND] = updated.showLegend
            prefs[SettingsKeys.SHOW_TOTAL] = updated.showTotal
            prefs[SettingsKeys.TIMELINE_START_INDEX] = updated.timelineStartIndex
            prefs[SettingsKeys.TIMELINE_END_INDEX] = updated.timelineEndIndex
            prefs[SettingsKeys.TIMELINE_STEP_MINUTES] = updated.timelineStepMinutes
        }
    }
}