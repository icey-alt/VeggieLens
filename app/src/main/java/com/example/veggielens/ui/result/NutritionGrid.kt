package com.example.veggielens.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veggielens.data.model.VegetableEntity

@Composable
fun NutritionGrid(
    vegetable: VegetableEntity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "营养成分亮点 (每 100 克)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NutritionCard(
                value = vegetable.calories.replace("kcal", ""),
                label = "卡路里",
                unit = "kcal",
                modifier = Modifier.weight(1f)
            )
            NutritionCard(
                value = vegetable.protein.replace("g", ""),
                label = "蛋白质",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
            NutritionCard(
                value = vegetable.carbs.replace("g", ""),
                label = "碳水",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
            NutritionCard(
                value = vegetable.fiber.replace("g", ""),
                label = "纤维",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NutritionCard(
    value: String,
    label: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFFF1F8E9), shape = RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Normal
            )
        }
    }
}