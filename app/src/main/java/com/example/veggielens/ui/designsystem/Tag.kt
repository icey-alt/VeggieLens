package com.example.veggielens.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.ui.theme.VeggieLensTheme

enum class TagTone { Neutral, Primary, Tertiary }

/** 小标签：分类、匹配度、来源说明等。替代原来的 CategoryChip 和各处手写的小色块。 */
@Composable
fun VlTag(
    text: String,
    modifier: Modifier = Modifier,
    tone: TagTone = TagTone.Neutral,
    icon: ImageVector? = null,
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        TagTone.Neutral -> colors.surfaceContainerHigh to colors.onSurfaceVariant
        TagTone.Primary -> colors.primaryContainer to colors.onPrimaryContainer
        TagTone.Tertiary -> colors.tertiaryContainer to colors.onTertiaryContainer
    }
    VlTag(text = text, container = container, content = content, modifier = modifier, icon = icon)
}

/** 自定义颜色版本，用于压在图片上的标签。 */
@Composable
fun VlTag(
    text: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@PreviewLightDark
@Composable
private fun VlTagPreview() {
    VeggieLensTheme {
        Surface {
            Row(
                modifier = Modifier.padding(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                VlTag("十字花科")
                VlTag("匹配度 92%", tone = TagTone.Primary)
                VlTag("AI 生成", tone = TagTone.Tertiary, icon = Icons.Rounded.AutoAwesome)
            }
        }
    }
}
