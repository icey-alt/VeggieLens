package com.example.veggielens.ui.result

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.veggielens.R
import com.example.veggielens.description.DescriptionSource
import com.example.veggielens.ui.designsystem.VlCard
import com.example.veggielens.ui.theme.Spacing

private const val CollapsedLines = 6

/** 科普介绍卡片：小图标 + 标题，正文较长时默认折叠。 */
@Composable
fun DescriptionCard(
    description: String,
    source: DescriptionSource,
    modifier: Modifier = Modifier,
) {
    val isAiGenerated = source == DescriptionSource.DEEPSEEK
    var expanded by rememberSaveable(description) { mutableStateOf(false) }
    var overflowing by rememberSaveable(description) { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    VlCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(Spacing.md)
                .animateContentSize(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isAiGenerated) Icons.Rounded.AutoAwesome else Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = stringResource(
                        if (isAiGenerated) R.string.result_description_ai else R.string.result_description_local
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                )
            }

            Spacer(Modifier.height(Spacing.sm))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
                maxLines = if (expanded) Int.MAX_VALUE else CollapsedLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layout ->
                    if (!expanded) overflowing = layout.hasVisualOverflow
                },
            )

            if (overflowing || expanded) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.padding(top = Spacing.xxs),
                ) {
                    Text(
                        stringResource(
                            if (expanded) R.string.result_collapse else R.string.result_expand
                        )
                    )
                }
            } else {
                Spacer(Modifier.height(Spacing.sm))
            }

            Text(
                text = stringResource(
                    if (isAiGenerated) R.string.result_disclaimer_ai else R.string.result_disclaimer_local
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
