package dev.faizksen.kursor.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.faizksen.kursor.R
import dev.faizksen.kursor.alerts.navigation.AlertsRoute
import dev.faizksen.kursor.converter.navigation.ConverterRoute
import dev.faizksen.kursor.settings.navigation.SettingsRoute
import dev.faizksen.kursor.ui.KursorAppState
import dev.faizksen.kursor.watchlist.navigation.WatchlistRoute
import dev.faizksen.kursor.watchlist.navigation.watchlistScreen

@Composable
fun KursorNavHost(
    appState: KursorAppState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = appState.navController,
        startDestination = WatchlistRoute,
        modifier = modifier
    ) {
        watchlistScreen(
            onPairClick = { /*open pair*/ },
            onAddPairClick = { /*open create pair*/ }
        )
        composable<ConverterRoute> { PlaceholderScreen(stringResource(R.string.top_level_converter)) }
        composable<AlertsRoute> { PlaceholderScreen(stringResource(R.string.top_level_alerts)) }
        composable<SettingsRoute> { PlaceholderScreen(stringResource(R.string.top_level_more)) }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name)
    }
}