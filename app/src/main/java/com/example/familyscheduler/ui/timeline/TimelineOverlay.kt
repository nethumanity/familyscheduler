package com.example.familyscheduler.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.ui.state.SettingsUiState

@Composable
fun TimelineOverlay(
    settings: SettingsUiState,
    slotsByPersonState: Map<Pair<Person, SlotState>, List<TimeSlot>>,
    modifier: Modifier = Modifier
) {
    if (!settings.showLegend && !settings.showTotal) return

    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.9f))
            .border(1.dp, Color.Gray)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        if (settings.showTotal) {
            TotalSection(slotsByPersonState)
        }

        if (settings.showLegend) {
            LegendSection()
        }
    }
}