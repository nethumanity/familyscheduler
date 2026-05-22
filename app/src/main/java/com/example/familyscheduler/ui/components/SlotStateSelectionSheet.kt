package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.ui.presentation.SlotStatePresentation
import java.time.LocalTime

@Composable
fun SlotStateSelectionSheet(
    time: LocalTime,
    person: Person,
    onSelect: (SlotState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${time} / ${person.label}",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        SlotState.selectionSheetAllowedState
            .forEach { state ->
                Text(
                    text = SlotStatePresentation.label(state),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(state) }
                        .padding(vertical = 12.dp)
                )
            }
    }
}
