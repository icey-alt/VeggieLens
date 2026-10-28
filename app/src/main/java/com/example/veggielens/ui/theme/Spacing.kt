package com.example.veggielens.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 间距只用这几档；页面左右边距统一 [Spacing.screen]。 */
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp

    val screen = md
}

/**
 * 相机取景卡片上的颜色。
 * 取景画面上无论深浅色模式都需要固定的黑白半透明色，所以不走 ColorScheme，
 * 但也只允许在这里定义一次。
 */
object ScanColors {
    val Background = Color.Black
    val Scrim = Color.Black.copy(alpha = 0.18f)
    val HintContainer = Color.Black.copy(alpha = 0.55f)
    val OnHint = Color.White
    val Bracket = Color.White
    val SideButton = Color.White.copy(alpha = 0.88f)
    val OnSideButton = Color(0xFF1A1C19)
    val SideButtonActive = Color(0xFFFFD54F)
    val ShutterRing = Color.White
    val ShutterFill = Color.White
}
