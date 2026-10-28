package com.example.veggielens.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.ui.theme.VeggieLensTheme

/**
 * 白底 + 1dp 浅灰描边的卡片，没有阴影。全应用的卡片都用它，保证风格一致。
 *
 * @param onClick 不为 null 时整张卡片可点击（带水波纹）
 */
@Composable
fun VlCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val border = BorderStroke(1.dp, colors.outlineVariant)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = colors.surfaceContainerLowest,
            border = border,
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = colors.surfaceContainerLowest,
            border = border,
        ) {
            Column(content = content)
        }
    }
}

@PreviewLightDark
@Composable
private fun VlCardPreview() {
    VeggieLensTheme {
        Surface {
            VlCard(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    text = "描边卡片",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(Spacing.md),
                )
            }
        }
    }
}
