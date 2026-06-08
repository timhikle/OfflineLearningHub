package com.offlinelearninghub.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.offlinelearninghub.R
import com.offlinelearninghub.ui.screens.DashboardScreen
import com.offlinelearninghub.ui.screens.LibraryScreen
import com.offlinelearninghub.ui.screens.PDFReaderScreen
import com.offlinelearninghub.ui.screens.PlayerScreen
import com.offlinelearninghub.ui.screens.SettingsScreen
import com.offlinelearninghub.ui.screens.TimerScreen
import com.offlinelearninghub.viewmodel.DashboardViewModel
import com.offlinelearninghub.viewmodel.LibraryViewModel
import com.offlinelearninghub.viewmodel.PDFReaderViewModel
import com.offlinelearninghub.viewmodel.PlayerViewModel
import com.offlinelearninghub.viewmodel.SettingsViewModel
import com.offlinelearninghub.viewmodel.TimerViewModel

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Library : Screen("library")
    data object Timer : Screen("timer")
    data object Settings : Screen("settings")
    data object Player : Screen("player/{fileId}") {
        fun createRoute(fileId: Long) = "player/$fileId"
    }
    data object PDFReader : Screen("pdf_reader/{fileId}") {
        fun createRoute(fileId: Long) = "pdf_reader/$fileId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, R.string.nav_dashboard, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Library, R.string.nav_library, Icons.Filled.LibraryBooks, Icons.Outlined.LibraryBooks),
    BottomNavItem(Screen.Timer, R.string.nav_timer, Icons.Filled.Timer, Icons.Outlined.Timer),
    BottomNavItem(Screen.Settings, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun MainNavGraph(
    dashboardViewModel: DashboardViewModel,
    libraryViewModel: LibraryViewModel,
    timerViewModel: TimerViewModel,
    settingsViewModel: SettingsViewModel,
    playerViewModel: PlayerViewModel,
    pdfReaderViewModel: PDFReaderViewModel,
    onLanguageChanged: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = bottomNavItems.map { it.screen.route }
    val showBottomBar = currentDestination?.route in bottomBarScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(item.labelRes),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToPlayer = { fileId ->
                        navController.navigate(Screen.Player.createRoute(fileId))
                    },
                    onNavigateToLibrary = {
                        navController.navigate(Screen.Library.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onNavigateToPlayer = { fileId ->
                        navController.navigate(Screen.Player.createRoute(fileId))
                    },
                    onNavigateToPdfReader = { fileId ->
                        navController.navigate(Screen.PDFReader.createRoute(fileId))
                    }
                )
            }

            composable(Screen.Timer.route) {
                TimerScreen(viewModel = timerViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onLanguageChanged = onLanguageChanged
                )
            }

            composable(
                route = Screen.Player.route,
                arguments = listOf(navArgument("fileId") { type = NavType.LongType })
            ) { backStackEntry ->
                val fileId = backStackEntry.arguments?.getLong("fileId") ?: return@composable
                PlayerScreen(
                    viewModel = playerViewModel,
                    fileId = fileId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PDFReader.route,
                arguments = listOf(navArgument("fileId") { type = NavType.LongType })
            ) { backStackEntry ->
                val fileId = backStackEntry.arguments?.getLong("fileId") ?: return@composable
                PDFReaderScreen(
                    viewModel = pdfReaderViewModel,
                    fileId = fileId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
