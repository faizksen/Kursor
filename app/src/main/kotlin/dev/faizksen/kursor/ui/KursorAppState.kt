package dev.faizksen.kursor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import dev.faizksen.kursor.alerts.navigation.AlertsRoute
import dev.faizksen.kursor.converter.navigation.ConverterRoute
import dev.faizksen.kursor.navigation.TopLevelDestination
import dev.faizksen.kursor.settings.navigation.SettingsRoute
import dev.faizksen.kursor.watchlist.navigation.navigateToWatchlist
import kotlin.reflect.KClass

@Stable
class KursorAppState(
    val navController: NavHostController,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val current = currentDestination
            return TopLevelDestination.entries.firstOrNull { current.isRouteInHierarchy(it.route) }
        }

    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        val options = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        when (destination) {
            TopLevelDestination.RATES -> navController.navigateToWatchlist(options)
            TopLevelDestination.CONVERTER -> navController.navigate(ConverterRoute, options)
            TopLevelDestination.ALERTS -> navController.navigate(AlertsRoute, options)
            TopLevelDestination.MORE -> navController.navigate(SettingsRoute, options)
        }
    }
}

@Composable
fun rememberKursorAppState(
    navController: NavHostController = rememberNavController(),
): KursorAppState = remember(navController) {
    KursorAppState(navController)
}

private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any { it.hasRoute(route) } == true