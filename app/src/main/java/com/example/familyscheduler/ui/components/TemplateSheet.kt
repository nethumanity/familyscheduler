package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.viewmodel.TimelineViewModel

@Composable
fun TemplateSheet(
    viewModel: TimelineViewModel,
    person: Person,
    onAddClick: (Person) -> Unit,
    onEditTemplate: (String, Person) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onApplyTemplate: (DailyTemplate) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val templates = uiState.templates
        .filter { t -> t.person == person }

    var expandedMenuId by remember { mutableStateOf<String?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${person.label} のテンプレート",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            TextButton(onClick = { onAddClick(person) }) {
                Text("登録")
            }
        }

        LazyColumn {
            items(templates) { template ->

                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = template.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onApplyTemplate(template) }
                                .padding(end = 8.dp)
                        )

                        IconButton(
                            onClick = { expandedMenuId = template.id },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "menu",
                                tint = Color.LightGray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedMenuId == template.id,
                        onDismissRequest = { expandedMenuId = null },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        DropdownMenuItem(
                            text = { Text("編集") },
                            onClick = {
                                expandedMenuId = null
                                onEditTemplate(template.id, person)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("削除") },
                            onClick = {
                                expandedMenuId = null
                                onDeleteTemplate(template.id)
                            }
                        )
                    }
                }
            }
        }
    }
}