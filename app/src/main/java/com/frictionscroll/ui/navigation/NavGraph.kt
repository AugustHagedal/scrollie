package com.frictionscroll.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.frictionscroll.ui.config.BurstConfigScreen
import com.frictionscroll.ui.debug.DebugScreen
import com.frictionscroll.ui.home.HomeScreen
import com.frictionscroll.ui.permissions.PermissionsScreen
import com.frictionscroll.ui.picker.AppPickerScreen

object Routes {
    const val HOME = "home"
    const val APP_PICKER = "app_picker"
    const val BURST_CONFIG = "burst_config"
    const val PERMISSIONS = "permissions"
    const val DEBUG = "debug"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToAppPicker = { navController.navigate(Routes.APP_PICKER) },
                onNavigateToConfig = { navController.navigate(Routes.BURST_CONFIG) },
                onNavigateToPermissions = { navController.navigate(Routes.PERMISSIONS) },
                onNavigateToDebug = { navController.navigate(Routes.DEBUG) }
            )
        }
        composable(Routes.APP_PICKER) {
            AppPickerScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.BURST_CONFIG) {
            BurstConfigScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DEBUG) {
            DebugScreen(onBack = { navController.popBackStack() })
        }
    }
}
