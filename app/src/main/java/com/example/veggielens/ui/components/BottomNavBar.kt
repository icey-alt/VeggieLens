package com.example.veggielens.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.veggielens.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.veggielens.navigation.Screen


@Composable
fun BottomNavBar(
    bottomNavItems: List<BottomNavItem>,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentScreen.name == item.route,
                onClick = { onNavigate(Screen.valueOf(item.route)) },
                label = { Text(stringResource(item.titleResId)) },
                icon = {
                    Icon(
                        painter = painterResource(item.iconResId),
                        contentDescription = when (item.titleResId) {
                            R.string.icon_scan -> "扫描"
                            R.string.icon_history -> "记录"
                            R.string.icon_profile -> "我的"
                            else -> null
                        }
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}