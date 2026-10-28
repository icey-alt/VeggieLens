package com.example.veggielens.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.veggielens.R
import com.example.veggielens.navigation.Screen

data class BottomNavItem(
    val screen: Screen,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

/** 线性图标，选中态只改颜色（与参考设计一致）。 */
object NavigationConfig {
    val BottomNavItems = listOf(
        BottomNavItem(Screen.Scan, R.string.icon_scan, Icons.Outlined.CenterFocusWeak),
        BottomNavItem(Screen.Guide, R.string.icon_guide, Icons.AutoMirrored.Outlined.MenuBook),
        BottomNavItem(Screen.History, R.string.icon_history, Icons.Outlined.Schedule),
        BottomNavItem(Screen.Profile, R.string.icon_profile, Icons.Outlined.Person),
    )
}
