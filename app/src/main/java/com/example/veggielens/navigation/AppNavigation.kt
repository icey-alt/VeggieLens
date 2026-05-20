package com.example.veggielens.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.veggielens.R
import com.example.veggielens.ui.history.HistoryScreen
import com.example.veggielens.ui.profile.ProfileScreen
import com.example.veggielens.ui.result.ResultScreen
import com.example.veggielens.ui.scan.ScanScreen

enum class Screen(@StringRes val title: Int) {
    Scan(title = R.string.scan),
    History(title = R.string.history),
    Profile(title = R.string.profile),
    Result(title = R.string.result)
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scan.name,
        modifier = modifier.padding(innerPadding)
    ) {
        composable(route = Screen.Scan.name) {
            ScanScreen()
        }
        composable(route = Screen.History.name) {
            HistoryScreen()
        }
        composable(route = Screen.Result.name) {
            ResultScreen()
        }
        composable(route = Screen.Profile.name) {
            ProfileScreen()
        }
    }
}