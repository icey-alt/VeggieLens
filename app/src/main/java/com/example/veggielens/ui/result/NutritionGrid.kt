package com.example.veggielens.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.veggielens.R
import com.example.veggielens.data.model.VegetableEntity
import com.example.veggielens.ui.designsystem.VlCard
import com.example.veggielens.ui.theme.Spacing

/** 营养亮点卡片：一行四个浅绿小格，数值用主色。 */
@Composable
fun NutritionCard(
    vegetable: VegetableEntity,
    modifier: Modifier = Modifier,
) {
    val calories = NutritionAmount.parse(vegetable.calories, defaultUnit = "kcal")
    val protein = NutritionAmount.parse(vegetable.protein, defaultUnit = "g")
    val carbs = NutritionAmount.parse(vegetable.carbs, defaultUnit = "g")
    val fiber = NutritionAmount.parse(vegetable.fiber, defaultUnit = "g")

    VlCard(modifier = modifier) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = stringResource(R.string.result_nutrition_card_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                NutrientTile(
                    value = calories.value,
                    label = stringResource(R.string.nutrition_calories_kcal),
                    modifier = Modifier.weight(1f),
                )
                NutrientTile(
                    value = protein.value + protein.unit,
                    label = stringResource(R.string.nutrition_protein),
                    modifier = Modifier.weight(1f),
                )
                NutrientTile(
                    value = carbs.value + carbs.unit,
                    label = stringResource(R.string.nutrition_carbs),
                    modifier = Modifier.weight(1f),
                )
                NutrientTile(
                    value = fiber.value + fiber.unit,
                    label = stringResource(R.string.nutrition_fiber),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NutrientTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(colors.primaryContainer)
            .padding(vertical = Spacing.sm, horizontal = Spacing.xxs)
            .clearAndSetSemantics { contentDescription = "$label $value" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * 把数据库里 "34kcal"、"2.8 g" 这样的字符串拆成数值和单位。
 * 只在 UI 层解析，不改 Room 表结构。
 */
internal data class NutritionAmount(val value: String, val unit: String) {
    companion object {
        private val pattern = Regex("""^\s*([0-9]+(?:[.,][0-9]+)?)\s*([A-Za-zμ]*)\s*$""")

        fun parse(raw: String, defaultUnit: String): NutritionAmount {
            val match = pattern.find(raw) ?: return NutritionAmount(raw.trim(), "")
            val unit = match.groupValues[2].ifBlank { defaultUnit }
            return NutritionAmount(match.groupValues[1], unit)
        }
    }
}
