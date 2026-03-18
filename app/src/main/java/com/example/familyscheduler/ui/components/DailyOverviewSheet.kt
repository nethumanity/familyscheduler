package com.example.familyscheduler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.evaluation.AvailabilityEvaluation
import com.example.familyscheduler.domain.evaluation.AvailabilityState
import com.example.familyscheduler.domain.evaluation.MissingReason
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.HouseholdRequirement
import com.example.familyscheduler.domain.requirement.TimeRangeHouseholdRequirement
import com.example.familyscheduler.domain.slot.TimeSlot
import com.example.familyscheduler.domain.time.TimeAxis
import java.time.LocalDate

@Composable
fun DailyOverviewSheet(
    date: LocalDate,
    //slots: List<TimeSlot>,
    requirements: List<HouseholdRequirement>,
    evaluations: List<AvailabilityEvaluation>,
    onDeleteSlot: (TimeSlot) -> Unit,
    onWarningClick: (Int) -> Unit
) {
    fun indexToTime(index: Int): String {
        return TimeAxis.all.getOrNull(index)?.toString() ?: "--:--"
    }

    LazyColumn(
        //modifier = Modifier.fillMaxHeight(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("📅 ${date}", fontWeight = FontWeight.Bold)

        }
        item {
            Spacer(Modifier.height(8.dp))
        }

        // =========================
        // 警告一覧
        // =========================
        val warnings = evaluations.filter {
            it.state == AvailabilityState.WARN
        }

        if (warnings.isNotEmpty()) {

            item {
                Text("⚠ 警告", fontWeight = FontWeight.Bold)

            }
            items(warnings) { eval ->

                val summary = eval.reasons.joinToString(" / ") {
                    MissingReason.renderMissingReasonSummary(it)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWarningClick(eval.index) }
                        .padding(vertical = 4.dp)
                ) {
                    Text("${indexToTime(eval.index)} $summary")
                }
            }
        }

        // =========================
        // 予定一覧（簡易）
        // =========================
        data class RequirementUiModel(
            val name: String,
            val startIndex: Int,
            val person: Set<Person>
        )

        val uiRequirements = requirements.mapNotNull {
            if (it is TimeRangeHouseholdRequirement) {
                RequirementUiModel(
                    it.name,
                    it.startIndex,
                    it.allowedPersons
                )
            } else null
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("■ 予定一覧", fontWeight = FontWeight.Bold)
        }
        items(uiRequirements
                .filter { it.name != "" }.sortedBy { it.startIndex }
        ) { req ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    indexToTime(req.startIndex),
                    modifier = Modifier.width(60.dp)
                )

                Text("${req.name}（${req.person.joinToString()}）")

                Spacer(Modifier.weight(1f))

                Text(
                    "削除",
                    color = Color.Red,
                    modifier = Modifier.clickable {
                        // 👉 この場合は全員分削除 or 個別削除設計必要
                    }
                )
            }
        }
    }
}