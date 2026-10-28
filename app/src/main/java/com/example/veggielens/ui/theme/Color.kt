package com.example.veggielens.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * VeggieLens 调色板：白底 + 中绿点缀 + 浅绿标签。
 *
 * - Green   主色，#2E7D32（白字对比度约 5:1）；GreenTint 是浅绿标签/徽章底色
 * - Neutral 带一点绿调的灰，由 Material HCT 算法生成（hue = 主色色相，chroma 3），
 *           数字后缀是色调（tone，0 = 黑，100 = 白）
 * - Carrot  胡萝卜橙，保留为 tertiary
 *
 * 页面代码不要直接引用这些常量，一律通过 MaterialTheme.colorScheme 取色。
 */

// 主色
internal val Green = Color(0xFF2E7D32)
internal val GreenDeep = Color(0xFF1B5E20)
internal val GreenTint = Color(0xFFE8F5E9)
internal val Green20 = Color(0xFF003909)
internal val Green30 = Color(0xFF005312)
internal val Green80 = Color(0xFF88D982)
internal val Green90 = Color(0xFFA3F69C)

// 次要色（鼠尾草绿）
internal val Sage10 = Color(0xFF111F0F)
internal val Sage20 = Color(0xFF253423)
internal val Sage30 = Color(0xFF3B4B38)
internal val Sage40 = Color(0xFF52634F)
internal val Sage80 = Color(0xFFBACCB3)
internal val Sage90 = Color(0xFFD6E8CE)

// 第三色（胡萝卜橙）
internal val Carrot10 = Color(0xFF351000)
internal val Carrot20 = Color(0xFF571F00)
internal val Carrot30 = Color(0xFF7B2F00)
internal val Carrot40 = Color(0xFF9F4206)
internal val Carrot80 = Color(0xFFFFB694)
internal val Carrot90 = Color(0xFFFFDBCC)

// 错误色
internal val Red10 = Color(0xFF410002)
internal val Red20 = Color(0xFF690005)
internal val Red30 = Color(0xFF93000A)
internal val Red40 = Color(0xFFBA1A1A)
internal val Red80 = Color(0xFFFFB4AB)
internal val Red90 = Color(0xFFFFDAD6)

// 中性灰
internal val Neutral4 = Color(0xFF0D0F0C)
internal val Neutral6 = Color(0xFF121411)
internal val Neutral10 = Color(0xFF1A1C19)
internal val Neutral12 = Color(0xFF1E201D)
internal val Neutral17 = Color(0xFF292A28)
internal val Neutral20 = Color(0xFF2F312E)
internal val Neutral22 = Color(0xFF343532)
internal val Neutral24 = Color(0xFF383A37)
internal val Neutral87 = Color(0xFFDADAD6)
internal val Neutral90 = Color(0xFFE3E3DE)
internal val Neutral93 = Color(0xFFECEBE6)
internal val Neutral95 = Color(0xFFF1F1EC)
internal val Neutral96 = Color(0xFFF4F4EF)
internal val Neutral97 = Color(0xFFF7F7F2)
internal val Neutral100 = Color(0xFFFFFFFF)

internal val NeutralVariant30 = Color(0xFF444841)
internal val NeutralVariant40 = Color(0xFF5B6059)
internal val NeutralVariant50 = Color(0xFF747871)
internal val NeutralVariant60 = Color(0xFF8E928A)
internal val NeutralVariant80 = Color(0xFFC4C8BF)
internal val NeutralVariant90 = Color(0xFFE0E4DB)
