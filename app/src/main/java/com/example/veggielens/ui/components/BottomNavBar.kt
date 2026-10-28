package com.example.veggielens.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.veggielens.navigation.Screen

/**
 * 底部导航栏：所有页面同一个样式。
 * 白底 + 顶部细分隔线；选中项只用主色（图标和文字变绿），不加胶囊指示器。
 */
@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = colors.primary,
        selectedTextColor = colors.primary,
        indicatorColor = Color.Transparent,
        unselectedIconColor = colors.onSurfaceVariant,
        unselectedTextColor = colors.onSurfaceVariant,
    )

    Column(modifier = modifier) {
        HorizontalDivider(color = colors.outlineVariant)
        NavigationBar(
            containerColor = colors.surface,
            tonalElevation = 0.dp,
        ) {
            items.forEach { item ->
                val selected = currentScreen == item.screen
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.screen) },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.icon,
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = itemColors,
                )
            }
        }
    }
}
