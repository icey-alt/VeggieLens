package com.example.veggielens.ui.result

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.veggielens.R
import com.example.veggielens.data.model.VegetableCatalog
import com.example.veggielens.data.model.VegetableEntity
import com.example.veggielens.ui.designsystem.TagTone
import com.example.veggielens.ui.designsystem.VegetableImage
import com.example.veggielens.ui.designsystem.VegetableThumbnail
import com.example.veggielens.ui.designsystem.VlCard
import com.example.veggielens.ui.designsystem.VlTag
import com.example.veggielens.ui.designsystem.assetImageModel
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.viewmodel.VegetableMatch

/**
 * 蔬菜主卡片（结果页、图鉴详情页共用）：大图（右上角徽章）+ 名称 + 学名 + 分类标签，
 * 有候选时在底部显示“也可能是”。
 *
 * @param badgeText 图片右上角的徽章文字，如“匹配 95%”“已发现”；null 不显示
 * @param showLowConfidenceHint 是否显示“不太确定”的提示
 */
@Composable
fun ResultHeroCard(
    vegetable: VegetableEntity,
    badgeText: String?,
    modifier: Modifier = Modifier,
    badgeTone: TagTone = TagTone.Primary,
    showLowConfidenceHint: Boolean = false,
    alternatives: List<VegetableMatch> = emptyList(),
    onSelectAlternative: (VegetableMatch) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme

    VlCard(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(Spacing.sm),
        ) {
            VegetableImage(
                model = assetImageModel(vegetable.assetImagePath),
                contentDescription = vegetable.chineseName,
                modifier = Modifier.fillMaxSize(),
            )
            if (badgeText != null) {
                VlTag(
                    text = badgeText,
                    tone = badgeTone,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.xs),
                )
            }
        }

        Column(
            modifier = Modifier.padding(
                start = Spacing.md,
                end = Spacing.md,
                top = Spacing.xxs,
                bottom = Spacing.md,
            ),
        ) {
            Text(
                text = vegetable.chineseName,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
            )
            Text(
                text = "${vegetable.scientificName} · ${vegetable.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            if (showLowConfidenceHint) {
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    text = stringResource(R.string.result_confidence_low),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.tertiary,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                VlTag(text = stringResource(R.string.result_tag_vegetable), tone = TagTone.Primary)
                VlTag(text = VegetableCatalog.category(vegetable.name), tone = TagTone.Primary)
            }

            if (alternatives.isNotEmpty()) {
                HorizontalDivider(
                    color = colors.outlineVariant,
                    modifier = Modifier.padding(vertical = Spacing.md),
                )
                AlternativesRow(alternatives = alternatives, onSelect = onSelectAlternative)
            }
        }
    }
}

/** “也可能是”：识别模型给出的其他候选，点击切换结果。 */
@Composable
fun AlternativesRow(
    alternatives: List<VegetableMatch>,
    onSelect: (VegetableMatch) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.result_alternatives),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xs))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            alternatives.forEach { alternative ->
                Surface(
                    onClick = { onSelect(alternative) },
                    shape = CircleShape,
                    color = colors.surfaceContainerHigh,
                ) {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = Spacing.sm, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        VegetableThumbnail(
                            englishName = alternative.vegetable.name,
                            contentDescription = null,
                            size = 28.dp,
                            modifier = Modifier.clip(CircleShape),
                        )
                        Text(
                            text = alternative.vegetable.chineseName,
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "${alternative.confidence}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
