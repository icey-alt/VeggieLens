package com.example.veggielens.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.veggielens.R
import com.example.veggielens.navigation.Screen

data class BottomNavItem(
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int,
    val route: String
)

object NavigationConfig {
    val BottomNavItems = listOf(
        BottomNavItem(titleResId = R.string.icon_scan, iconResId = R.drawable.icon_scan, route = Screen.Scan.name),
        BottomNavItem(titleResId = R.string.icon_history , iconResId = R.drawable.icon_history , route = Screen.History.name),
        BottomNavItem(titleResId = R.string.icon_profile , iconResId = R.drawable.icon_profile, route = Screen.Profile.name),
    )
}