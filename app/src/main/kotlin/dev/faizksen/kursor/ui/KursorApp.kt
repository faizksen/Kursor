package dev.faizksen.kursor.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.window.core.layout.WindowSizeClass
import dev.faizksen.kursor.navigation.KursorNavHost
import dev.faizksen.kursor.navigation.TopLevelDestination

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun KursorApp(
    appState: KursorAppState = rememberKursorAppState(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
) {
    val currentDestination = appState.currentTopLevelDestination
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                    label = { Text(stringResource(destination.labelRes)) },
                    selected = destination == currentDestination,
                    onClick = { appState.navigateToTopLevelDestination(destination) }
                )
            }
        },
        layoutType = if (currentDestination == null) {
            NavigationSuiteType.None
        } else {
            navigationTypeFor(windowAdaptiveInfo)
        }
    ) {
        KursorNavHost(appState = appState)
    }
}

private fun navigationTypeFor(adaptiveInfo: WindowAdaptiveInfo): NavigationSuiteType =
    if (adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        NavigationSuiteType.WideNavigationRailCollapsed
    } else {
        NavigationSuiteType.ShortNavigationBarCompact
    }