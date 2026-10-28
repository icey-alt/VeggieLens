package com.example.veggielens.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * 圆角只有三档：
 * small  8dp  Chip、标签
 * medium 16dp 列表项、缩略图、数字格子
 * large  24dp 大卡片、底部抽屉、取景框
 */
val VeggieShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
