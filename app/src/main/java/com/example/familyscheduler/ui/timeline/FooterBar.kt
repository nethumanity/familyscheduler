package com.example.familyscheduler.ui.timeline

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.R

@Composable
fun FooterBar() {
    NavigationBar {

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = "Calendar",
                    //tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("カレンダー") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_child),
                    contentDescription = "Child",
                    //tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("子ども") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
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
            onClick = {},
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
            onClick = {},
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
