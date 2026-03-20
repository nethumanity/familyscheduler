package com.example.familyscheduler.ui.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.R
import com.example.familyscheduler.ui.components.SpeechBubble
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun FooterBar(
    viewModel: TimelineViewModel,
    onOverviewClick: () -> Unit,
    onChildClick: () -> Unit,
    onTodayClick: () -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val guideState by viewModel.guideState.collectAsState()

    NavigationBar {

        NavigationBarItem(
            selected = false,
            onClick = onOverviewClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_summary),  //ic_calendar
                    contentDescription = "Calendar",
                    //tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("サマリー") }    //"カレンダー"
        )

        NavigationBarItem(
            selected = false,
            onClick = onChildClick,
            icon = {
                Box (
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_child),
                        contentDescription = "Child",
                        //tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )

                    if (guideState.showChildHint) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-15).dp)
                        ) {
                            SpeechBubble("まずはココ！")
                        }
                    }
                }
            },
            label = { Text("子ども") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onTodayClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_today),
                    contentDescription = "Today",
                    //tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("今日") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onAddClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "Add",
                    //tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("予定を追加") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "Settings",
                    //tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("設定") }
        )
    }
}
