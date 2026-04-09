package com.example.familyscheduler.ui.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HeaderBar(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern(
        "MM/dd (E)",
        Locale.JAPANESE
    )

    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            NavigationBarItem(
                selected = false,
                onClick = onPreviousDay,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_previousday),
                        contentDescription = "PreviousDay",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Text(
                text = date.format(formatter),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            NavigationBarItem(
                selected = false,
                onClick = onNextDay,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_nextday),
                        contentDescription = "NextDay",
                        modifier = Modifier.size(24.dp)
                    )}
            )
        }
    }
}
