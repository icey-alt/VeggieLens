package com.example.veggielens.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.veggielens.ui.components.BottomNavBar
import com.example.veggielens.ui.components.NavigationConfig
import com.example.veggielens.ui.components.TopNavBar


@Composable
fun VeggieLensApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: Screen.Scan.name

    Scaffold(
        topBar = {
            TopNavBar(
                currentScreen = Screen.valueOf(currentRoute),
                canNavigateBack = currentRoute == Screen.Result.name,
                navigateUp = { navController.navigateUp() },
                action = {  },
                modifier = Modifier
            )
        },
        bottomBar = {
            BottomNavBar(
                bottomNavItems = NavigationConfig.BottomNavItems,
                currentScreen = Screen.valueOf(currentRoute),
                onNavigate = { screen ->
                    if (screen == Screen.Scan && currentRoute == Screen.Result.name) {
                        navController.popBackStack(Screen.Scan.name, false)
                    } else {
                        navController.navigate(screen.name) {
                            popUpTo(Screen.Scan.name) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier
            )
        }
    ) { innerPadding ->
        AppNavigation(navController = navController, innerPadding = innerPadding)
    }
}