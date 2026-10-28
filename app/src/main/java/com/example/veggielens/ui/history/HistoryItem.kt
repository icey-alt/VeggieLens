package com.example.veggielens.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.veggielens.data.model.ScanHistory
import com.example.veggielens.data.model.VegetableCatalog
import com.example.veggielens.ui.designsystem.TagTone
import com.example.veggielens.ui.designsystem.VegetableThumbnail
import com.example.veggielens.ui.designsystem.VlCard
import com.example.veggielens.ui.designsystem.VlTag
import com.example.veggielens.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 记录列表项：白底描边卡片，左边蔬菜图，中间名称 / 学名 / 时间，右上角匹配度徽章。
 */
@Composable
fun HistoryItem(
    history: ScanHistory,
    scientificName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chineseName = VegetableCatalog.chineseName(history.vegetableName)
    val time = remember(history.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(history.timestamp))
    }
    val colors = MaterialTheme.colorScheme

    VlCard(onClick = onClick, modifier = modifier) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            VegetableThumbnail(
                englishName = history.vegetableName,
                contentDescription = null,
                size = 72.dp,
            )
            Spacer(Modifier.width(Spacing.md))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
            ) {
                Text(
                    text = chineseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = scientificName ?: history.vegetableName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            VlTag(text = "${history.confidence}%", tone = TagTone.Primary)
        }
    }
}

/** 历史记录按“今天 / 昨天 / 具体日期”分组。 */
internal object HistoryDateGrouping {
    data class Section(val key: String, val label: String, val items: List<ScanHistory>)

    fun group(
        history: List<ScanHistory>,
        todayLabel: String,
        yesterdayLabel: String,
        now: Long = System.currentTimeMillis(),
    ): List<Section> {
        val today = Calendar.getInstance().apply { timeInMillis = now }
        val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val sameYearFormat = SimpleDateFormat("M月d日", Locale.getDefault())
        val otherYearFormat = SimpleDateFormat("yyyy年M月d日", Locale.getDefault())

        return history
            .groupBy { dayKey(it.timestamp) }
            .map { (key, items) ->
                val day = Calendar.getInstance().apply { timeInMillis = items.first().timestamp }
                val label = when {
                    day.isSameDay(today) -> todayLabel
                    day.isSameDay(yesterday) -> yesterdayLabel
                    day.get(Calendar.YEAR) == today.get(Calendar.YEAR) ->
                        sameYearFormat.format(day.time)
                    else -> otherYearFormat.format(day.time)
                }
                Section(key = key, label = label, items = items)
            }
    }

    private fun dayKey(timestamp: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = timestamp }
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}
