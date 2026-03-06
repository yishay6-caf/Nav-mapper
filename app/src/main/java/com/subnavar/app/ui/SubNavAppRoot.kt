package com.subnavar.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subnavar.app.ui.floorplan.FloorPlanScreen
import com.subnavar.app.ui.mapping.MappingScreen
import com.subnavar.app.ui.navigation.NavigationScreen
import com.subnavar.app.ui.settings.SettingsScreen
import com.subnavar.app.ui.streetview.StreetViewScreen
import com.subnavar.app.util.FileLogger
import com.subnavar.app.util.LocaleManager
import com.subnavar.app.util.Strings
import com.subnavar.app.util.Strings.get

sealed class Screen(val route: String) {
    data object FloorPlan : Screen("floorplan")
    data object Navigate : Screen("navigate")
    data object Settings : Screen("settings")
    data object Mapping : Screen("mapping/{buildingId}/{floorId}") {
        fun createRoute(buildingId: Long, floorId: Long) = "mapping/$buildingId/$floorId"
    }
    data object StreetView : Screen("streetview/{waypointId}") {
        fun createRoute(waypointId: Long) = "streetview/$waypointId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val labelEn: String,
    val labelHe: String,
    val icon: ImageVector
)

@Composable
fun SubNavAppRoot() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val lang = remember { LocaleManager.getLanguage(context) }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.FloorPlan, "Map", "מפה", Icons.Default.Map),
        BottomNavItem(Screen.Navigate, "Navigate", "ניווט", Icons.Default.Navigation),
        BottomNavItem(Screen.Settings, "Settings", "הגדרות", Icons.Default.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.FloorPlan.route,
        Screen.Navigate.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = {
                                Text(
                                    if (lang == LocaleManager.AppLanguage.HEBREW) item.labelHe
                                    else item.labelEn
                                )
                            },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.FloorPlan.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.FloorPlan.route) {
                FileLogger.log("NAV", "Composing FloorPlanScreen")
                FloorPlanScreen(
                    onNavigateToMapping = { buildingId, floorId ->
                        FileLogger.log("NAV", "Navigate to Mapping: building=$buildingId, floor=$floorId")
                        navController.navigate(Screen.Mapping.createRoute(buildingId, floorId))
                    },
                    onNavigateToStreetView = { waypointId ->
                        FileLogger.log("NAV", "Navigate to StreetView: waypoint=$waypointId")
                        navController.navigate(Screen.StreetView.createRoute(waypointId))
                    }
                )
            }
            composable(Screen.Navigate.route) {
                FileLogger.log("NAV", "Composing NavigationScreen")
                NavigationScreen(
                    onNavigateToStreetView = { waypointId ->
                        FileLogger.log("NAV", "Navigate to StreetView from Nav: waypoint=$waypointId")
                        navController.navigate(Screen.StreetView.createRoute(waypointId))
                    }
                )
            }
            composable(Screen.Settings.route) {
                FileLogger.log("NAV", "Composing SettingsScreen")
                SettingsScreen()
            }
            composable(
                route = Screen.Mapping.route,
                arguments = listOf(
                    navArgument("buildingId") { type = NavType.LongType },
                    navArgument("floorId") { type = NavType.LongType }
                )
            ) {
                MappingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.StreetView.route,
                arguments = listOf(
                    navArgument("waypointId") { type = NavType.LongType }
                )
            ) {
                StreetViewScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
