package com.example.veggielens.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.veggielens.R
import com.example.veggielens.ui.history.HistoryScreen
import com.example.veggielens.ui.profile.ProfileScreen
import com.example.veggielens.ui.result.ResultScreen
import com.example.veggielens.ui.scan.ScanScreen
import com.example.veggielens.viewmodel.HistoryViewModel
import com.example.veggielens.viewmodel.ProfileViewModel
import com.example.veggielens.viewmodel.ScanViewModel

enum class Screen(@param:StringRes val title: Int) {
    Scan(title = R.string.scan),
    History(title = R.string.history),
    Profile(title = R.string.profile),
    Result(title = R.string.result)
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues
) {
    val scanViewModel: ScanViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Scan.name,
        modifier = modifier.padding(innerPadding)
    ) {
        composable(route = Screen.Scan.name) {
            ScanScreen(
                viewModel = scanViewModel,
                onNavigateToResult = { navController.navigate(Screen.Result.name) }
            )
        }
        composable(route = Screen.History.name) {
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateToScan = {
                    navController.navigate(Screen.Scan.name) {
                        popUpTo(Screen.Scan.name) { inclusive = true }
                    }
                },
                onNavigateToResult = { history ->
                    scanViewModel.loadResultFromHistory(history)
                    navController.navigate(Screen.Result.name)
                }
            )
        }
        composable(route = Screen.Result.name) {
            ResultScreen(
                viewModel = scanViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(route = Screen.Profile.name) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                historyViewModel = historyViewModel
            )
        }
    }
}