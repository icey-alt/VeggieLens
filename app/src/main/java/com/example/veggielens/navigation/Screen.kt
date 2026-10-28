package com.example.veggielens.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.veggielens.ui.components.BottomNavBar
import com.example.veggielens.ui.components.NavigationConfig

/**
 * 应用外壳：底部导航在所有页面都显示且样式一致。
 * 顶栏由各页面自己的 Scaffold 管理，页面通过 contentPadding 拿到底栏高度。
 */
@Composable
fun VeggieLensApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: Screen.Scan.name
    val currentScreen = Screen.fromRoute(currentRoute)
    // 详情页属于“图鉴”标签，结果页不属于任何标签
    val selectedTab = if (currentScreen == Screen.Detail) Screen.Guide else currentScreen

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavBar(
                items = NavigationConfig.BottomNavItems,
                currentScreen = selectedTab,
                onNavigate = { screen ->
                    when {
                        screen == currentScreen -> Unit
                        // 从详情页点“图鉴”：回到图鉴列表
                        screen == Screen.Guide && currentScreen == Screen.Detail ->
                            navController.popBackStack(Screen.Guide.name, false)
                        screen == Screen.Scan && currentScreen == Screen.Result ->
                            navController.popBackStack(Screen.Scan.name, false)
                        else -> navController.navigateToTab(screen)
                    }
                },
            )
        },
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            contentPadding = innerPadding,
        )
    }
}
