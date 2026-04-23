package com.example.familyscheduler.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object SettingsKeys {
    val MAX_CHILDREN_PER_ADULT = intPreferencesKey("max_children_per_adult")
    val BEDTIME_STEPS = intPreferencesKey("bedtime_steps")
    val DROP_OFF_STEPS = intPreferencesKey("drop_off_steps")
    val PICKUP_STEPS = intPreferencesKey("pickup_steps")

    val SHOW_LEGEND = booleanPreferencesKey("show_legend")
    val SHOW_TOTAL = booleanPreferencesKey("show_total")

    val TIMELINE_START_INDEX = intPreferencesKey("timeline_start_index")
    val TIMELINE_END_INDEX = intPreferencesKey("timeline_end_index")
    val TIMELINE_STEP_MINUTES = intPreferencesKey("timeline_step_minutes")
}