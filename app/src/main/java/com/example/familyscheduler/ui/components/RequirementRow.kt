package com.example.familyscheduler.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.ui.utilities.RequirementUiModel

@Composable
fun RequirementRow(
    req: RequirementUiModel,
    isWarn: Boolean,
    count: String?,
    assignedPersons: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val baseColor =
        if (req.mode == RequirementModeToday.CANCELED) Color.LightGray
        else Color.Unspecified

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            TimeAxis.all.getOrNull(req.startIndex)?.toString() ?: "--:--",
            modifier = Modifier.width(60.dp),
            color = baseColor
        )

        Text(req.name, color = baseColor)

        Spacer(Modifier.weight(1f))

        when (req.mode) {
            RequirementModeToday.AUTO -> {
                if (!isWarn) {
                    Text("✔ $assignedPersons", color = Color.Green)
                } else {
                    Text("⚠ $count", color = Color.Red)
                }
            }

            RequirementModeToday.REVERSE -> {
                if (!isWarn) {
                    Text("➥ $assignedPersons", color = Color.Blue)
                }
            }

            RequirementModeToday.CANCELED -> {
                Text("キャンセル", color = Color.LightGray)
            }
        }
        //ケバブメニューを追加（タップで現在の削除・編集メニューが展開される）
        //ケバブメニュー表示条件は、rulesのsource = RequirementSource.USERの場合
        //（おそらく、RequirementUiModelにsourceを追加すべき）
        //RowにもonClickのクリッカブル機能があるので、Rowとメニューの境界をなるべくわかりやすくする
    }
}