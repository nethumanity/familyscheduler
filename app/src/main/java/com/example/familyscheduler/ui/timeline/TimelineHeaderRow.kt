@file:OptIn(ExperimentalFoundationApi::class)

package com.example.familyscheduler.ui.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.ui.components.SpeechBubble
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun TimelineHeaderRow(
    viewModel: TimelineViewModel,
    persons: List<Person>,
    dailyStates: List<DailyState>,
    onDailyStateClick: (Person) -> Unit
) {
    val guideState by viewModel.guideState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFFF5F5F5))
            .border(0.5.dp, Color.LightGray)
    ) {
        Spacer(modifier = Modifier.width(64.dp))

        persons.forEach { person ->

            val showHint = when (person) {
                Person.FATHER -> guideState.showFatherHint
                Person.MOTHER -> guideState.showMotherHint
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(0.5.dp, Color.DarkGray)
                    .clickable{ onDailyStateClick(person) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box{
                    Text(
                        text = person.label,
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    if (showHint) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = 20.dp, y = 0.dp)
                        ) {
                            SpeechBubble("まずはココ！")
                        }
                    }
                }

                Text(
                    text = dailyStates.firstOrNull { it.person  == person }
                        ?.templateName ?: "No Template",
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
