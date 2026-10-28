package com.example.veggielens.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.veggielens.R
import com.example.veggielens.ui.guide.GuideScreen
import com.example.veggielens.ui.guide.VegetableDetailScreen
import com.example.veggielens.ui.history.HistoryScreen
import com.example.veggielens.ui.profile.ProfileScreen
import com.example.veggielens.ui.result.ResultScreen
import com.example.veggielens.ui.scan.ScanScreen
import com.example.veggielens.viewmodel.GuideViewModel
import com.example.veggielens.viewmodel.HistoryViewModel
import com.example.veggielens.viewmodel.ProfileViewModel
import com.example.veggielens.viewmodel.ScanViewModel

enum class Screen(@param:StringRes val title: Int) {
    Scan(title = R.string.scan),
    Guide(title = R.string.guide),
    History(title = R.string.history),
    Profile(title = R.string.profile),
    Result(title = R.string.result),
    Detail(title = R.string.detail);

    companion object {
        /** 由路由字符串（可能带参数，如 "Detail/{name}"）得到页面 */
        fun fromRoute(route: String?): Screen =
            entries.firstOrNull { it.name == route?.substringBefore('/') } ?: Scan
    }
}

private const val DetailNameArg = "name"
private val DetailRoute = "${Screen.Detail.name}/{$DetailNameArg}"

private fun detailRoute(vegetableName: String) = "${Screen.Detail.name}/${Uri.encode(vegetableName)}"

/** 切换底部标签：保留各标签的页面状态，避免返回栈无限增长。 */
internal fun NavHostController.navigateToTab(screen: Screen) {
    navigate(screen.name) {
        popUpTo(Screen.Scan.name) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * 跳到某个标签的首页（如图鉴列表），不恢复该标签之前保存的子页面（如蔬菜详情）。
 * 用于页面内的入口按钮，例如「我的」页的“查看蔬菜图鉴”。
 */
internal fun NavHostController.navigateToTabRoot(screen: Screen) {
    navigate(screen.name) {
        popUpTo(Screen.Scan.name) { saveState = true }
        launchSingleTop = true
        restoreState = false
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val scanViewModel: ScanViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val guideViewModel: GuideViewModel = viewModel()

    val goToScan: () -> Unit = {
        if (!navController.popBackStack(Screen.Scan.name, inclusive = false)) {
            navController.navigate(Screen.Scan.name)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Scan.name,
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
    ) {
        composable(route = Screen.Scan.name) {
            ScanScreen(
                viewModel = scanViewModel,
                contentPadding = contentPadding,
                onNavigateToResult = { navController.navigate(Screen.Result.name) },
            )
        }
        composable(route = Screen.Guide.name) {
            GuideScreen(
                viewModel = guideViewModel,
                contentPadding = contentPadding,
                onOpenDetail = { name -> navController.navigate(detailRoute(name)) },
            )
        }
        composable(
            route = DetailRoute,
            arguments = listOf(navArgument(DetailNameArg) { type = NavType.StringType }),
            enterTransition = { slideInVertically(tween(280)) { it / 10 } + fadeIn(tween(280)) },
            popExitTransition = { slideOutVertically(tween(220)) { it / 10 } + fadeOut(tween(220)) },
        ) { backStackEntry ->
            VegetableDetailScreen(
                viewModel = guideViewModel,
                vegetableName = backStackEntry.arguments?.getString(DetailNameArg).orEmpty(),
                contentPadding = contentPadding,
                onNavigateBack = { navController.navigateUp() },
                onGoScan = goToScan,
            )
        }
        composable(route = Screen.History.name) {
            HistoryScreen(
                viewModel = historyViewModel,
                contentPadding = contentPadding,
                onNavigateToScan = goToScan,
                onNavigateToResult = { history ->
                    scanViewModel.loadResultFromHistory(history)
                    navController.navigate(Screen.Result.name)
                },
            )
        }
        composable(
            route = Screen.Result.name,
            enterTransition = { slideInVertically(tween(280)) { it / 10 } + fadeIn(tween(280)) },
            popExitTransition = { slideOutVertically(tween(220)) { it / 10 } + fadeOut(tween(220)) },
        ) {
            ResultScreen(
                viewModel = scanViewModel,
                contentPadding = contentPadding,
                onNavigateBack = { navController.navigateUp() },
                onScanAgain = goToScan,
            )
        }
        composable(route = Screen.Profile.name) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                historyViewModel = historyViewModel,
                contentPadding = contentPadding,
                onOpenGuide = { navController.navigateToTabRoot(Screen.Guide) },
            )
        }
    }
}
