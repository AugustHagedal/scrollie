package com.frictionscroll.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.frictionscroll.ui.config.SettingsScreen
import com.frictionscroll.ui.debug.StatsScreen
import com.frictionscroll.ui.disclosure.DisclosureScreen
import com.frictionscroll.ui.home.HomeScreen
import com.frictionscroll.ui.permissions.PermissionsScreen
import com.frictionscroll.ui.picker.AppPickerScreen

object Routes {
    const val HOME = "home"
    const val APP_PICKER = "app_picker"
    const val SETTINGS = "settings"
    const val PERMISSIONS = "permissions"
    const val STATS = "stats"
    const val DISCLOSURE = "disclosure"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToAppPicker = { navController.navigate(Routes.APP_PICKER) },
                onNavigateToConfig = { navController.navigate(Routes.SETTINGS) },
                onNavigateToPermissions = { navController.navigate(Routes.PERMISSIONS) },
                onNavigateToStats = { navController.navigate(Routes.STATS) },
                onNavigateToDisclosure = { navController.navigate(Routes.DISCLOSURE) }
            )
        }
        composable(Routes.APP_PICKER) {
            AppPickerScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DISCLOSURE) {
            DisclosureScreen(onBack = { navController.popBackStack() })
        }
    }
}
