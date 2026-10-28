package com.example.veggielens.ui.result

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.veggielens.R
import com.example.veggielens.data.model.MicroNutrients
import com.example.veggielens.data.model.Nutrient
import com.example.veggielens.data.model.VegetableEntity
import com.example.veggielens.data.model.VegetableGuide
import com.example.veggielens.ui.designsystem.TagTone
import com.example.veggielens.ui.designsystem.VlCard
import com.example.veggielens.ui.designsystem.VlTag
import com.example.veggielens.ui.theme.Spacing
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 维生素与矿物质卡片：顶部是“富含 xx”标签，下面每种营养素一行（含量 + 占每日参考值的比例条）。
 */
@Composable
fun MicronutrientCard(
    vegetable: VegetableEntity,
    modifier: Modifier = Modifier,
) {
    val info = VegetableGuide.forName(vegetable.name)
    val nutrients = info?.nutrients
    val colors = MaterialTheme.colorScheme

    VlCard(modifier = modifier) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = stringResource(R.string.micro_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )

            if (nutrients == null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = stringResource(R.string.micro_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                return@Column
            }

            val rich = VegetableGuide.richNutrients(nutrients)
            if (rich.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    rich.forEach { nutrient ->
                        VlTag(
                            text = stringResource(R.string.micro_rich_in, stringResource(nutrient.labelRes)),
                            tone = TagTone.Primary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            Nutrient.entries.forEach { nutrient ->
                NutrientRow(nutrient = nutrient, nutrients = nutrients)
            }

            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = stringResource(R.string.micro_source),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NutrientRow(
    nutrient: Nutrient,
    nutrients: MicroNutrients,
) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(nutrient.labelRes)
    val amount = "${formatAmount(nutrient.amountIn(nutrients))} ${nutrient.unit}"
    val percent = nutrient.percentOfNrv(nutrients)
    val isRich = percent >= Nutrient.RICH_THRESHOLD_PERCENT
    val spoken = stringResource(R.string.micro_row_spoken, label, amount, percent)

    Column(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .clearAndSetSemantics { contentDescription = spoken },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelMedium,
                color = if (isRich) colors.primary else colors.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(52.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerHigh),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((percent / 100f).coerceIn(0.02f, 1f))
                    .clip(CircleShape)
                    .background(if (isRich) colors.primary else colors.outline),
            )
        }
    }
}

/** 选购与烹饪卡片：时令、挑选、保存、推荐做法。没有资料的蔬菜不显示。 */
@Composable
fun LifeTipsCard(
    vegetable: VegetableEntity,
    modifier: Modifier = Modifier,
) {
    val info = VegetableGuide.forName(vegetable.name) ?: return
    val colors = MaterialTheme.colorScheme

    VlCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.tips_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )
            TipRow(Icons.Outlined.CalendarMonth, stringResource(R.string.tips_season)) {
                TipText(info.season)
            }
            TipRow(Icons.Outlined.TaskAlt, stringResource(R.string.tips_selection)) {
                TipText(info.selection)
            }
            TipRow(Icons.Outlined.Kitchen, stringResource(R.string.tips_storage)) {
                TipText(info.storage)
            }
            TipRow(Icons.Outlined.Restaurant, stringResource(R.string.tips_dishes)) {
                Row(
                    modifier = Modifier
                        .padding(top = Spacing.xxs)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    info.dishes.forEach { dish -> VlTag(text = dish) }
                }
            }
        }
    }
}

@Composable
private fun TipRow(
    icon: ImageVector,
    label: String,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface,
            )
            content()
        }
    }
}

@Composable
private fun TipText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@get:StringRes
internal val Nutrient.labelRes: Int
    get() = when (this) {
        Nutrient.VitaminC -> R.string.nutrient_vitamin_c
        Nutrient.VitaminA -> R.string.nutrient_vitamin_a
        Nutrient.Potassium -> R.string.nutrient_potassium
        Nutrient.Calcium -> R.string.nutrient_calcium
        Nutrient.Iron -> R.string.nutrient_iron
        Nutrient.Folate -> R.string.nutrient_folate
    }

private fun formatAmount(value: Float): String = when {
    value >= 10f -> value.roundToInt().toString()
    value >= 1f -> String.format(Locale.US, "%.1f", value)
    else -> String.format(Locale.US, "%.2f", value)
}
