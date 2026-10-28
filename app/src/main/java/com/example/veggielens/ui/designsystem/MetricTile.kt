package com.example.veggielens.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.ui.theme.VeggieLensTheme

/**
 * 数字格子：营养成分、个人统计。替代原来的 NutritionCard 和 StatBox。
 *
 * @param emphasized 浅绿底高亮，每屏最多一个。
 */
@Composable
fun MetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    icon: ImageVector? = null,
    emphasized: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val container = if (emphasized) colors.primaryContainer else colors.surfaceContainerLowest
    val valueColor = if (emphasized) colors.onPrimaryContainer else colors.onSurface
    val labelColor = if (emphasized) colors.onPrimaryContainer else colors.onSurfaceVariant
    val spoken = listOfNotNull(label, value, unit).joinToString(" ")

    Surface(
        modifier = modifier.clearAndSetSemantics { contentDescription = spoken },
        color = container,
        shape = MaterialTheme.shapes.medium,
        border = if (emphasized) null else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(Spacing.xxs + 2.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(Spacing.xxs))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = valueColor,
                    maxLines = 1,
                )
                if (unit != null) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = labelColor,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MetricTilePreview() {
    VeggieLensTheme {
        Surface {
            Row(
                modifier = Modifier.padding(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                MetricTile(
                    value = "34",
                    unit = "kcal",
                    label = "热量",
                    icon = Icons.Rounded.LocalFireDepartment,
                    emphasized = true,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(value = "2.8", unit = "g", label = "蛋白质", modifier = Modifier.weight(1f))
            }
        }
    }
}
