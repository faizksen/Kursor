package dev.faizksen.kursor.watchlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.faizksen.kursor.watchlist.WatchlistScreen
import kotlinx.serialization.Serializable

@Serializable
data object WatchlistRoute

fun NavController.navigateToWatchlist(navOptions: NavOptions) = navigate(WatchlistRoute, navOptions)

fun NavGraphBuilder.watchlistScreen(
    onPairClick: (Long) -> Unit,
    onAddPairClick: () -> Unit,
    onSyncStatusClick: () -> Unit,
) {
    composable<WatchlistRoute> {
        WatchlistScreen(
            onPairClick = onPairClick,
            onAddPairClick = onAddPairClick,
            onSyncStatusClick = onSyncStatusClick,
        )
    }
}