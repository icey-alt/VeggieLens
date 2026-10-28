package com.example.veggielens.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*
 * 完整的浅色 / 深色 ColorScheme（白底卡片风格）。
 * 角色到色调的映射遵循 Material 3 规范（与 Now in Android 的 Theme.kt 一致），
 * 并补齐了 surfaceContainer* 五级，页面用它们区分层次，而不是给每张卡片加阴影。
 */
internal val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenTint,
    onPrimaryContainer = GreenDeep,
    inversePrimary = Green80,
    secondary = Sage40,
    onSecondary = Color.White,
    secondaryContainer = Sage90,
    onSecondaryContainer = Sage10,
    tertiary = Carrot40,
    onTertiary = Color.White,
    tertiaryContainer = Carrot90,
    onTertiaryContainer = Carrot10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Neutral100,
    onBackground = Neutral10,
    surface = Neutral100,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant40,
    surfaceTint = Green,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    outline = NeutralVariant50,
    outlineVariant = Neutral93,
    scrim = Color.Black,
    surfaceBright = Neutral100,
    surfaceDim = Neutral87,
    surfaceContainerLowest = Neutral100,
    surfaceContainerLow = Neutral97,
    surfaceContainer = Neutral96,
    surfaceContainerHigh = Neutral95,
    surfaceContainerHighest = Neutral90,
)

internal val DarkColors = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,
    inversePrimary = Green,
    secondary = Sage80,
    onSecondary = Sage20,
    secondaryContainer = Sage30,
    onSecondaryContainer = Sage90,
    tertiary = Carrot80,
    onTertiary = Carrot20,
    tertiaryContainer = Carrot30,
    onTertiaryContainer = Carrot90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Neutral6,
    onBackground = Neutral90,
    surface = Neutral6,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    surfaceTint = Green80,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    scrim = Color.Black,
    surfaceBright = Neutral24,
    surfaceDim = Neutral6,
    surfaceContainerLowest = Neutral4,
    surfaceContainerLow = Neutral10,
    surfaceContainer = Neutral12,
    surfaceContainerHigh = Neutral17,
    surfaceContainerHighest = Neutral22,
)

/**
 * VeggieLens 主题。
 *
 * @param dynamicColor Android 12+ 跟随壁纸取色。默认关闭，保证品牌绿一致
 * （Now in Android 同样默认 disableDynamicTheming = true）。
 */
@Composable
fun VeggieLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VeggieTypography,
        shapes = VeggieShapes,
        content = content,
    )
}
