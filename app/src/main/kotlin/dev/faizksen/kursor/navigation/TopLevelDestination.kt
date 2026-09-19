package dev.faizksen.kursor.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.vector.ImageVector
import dev.faizksen.kursor.R
import dev.faizksen.kursor.alerts.navigation.AlertsRoute
import dev.faizksen.kursor.converter.navigation.ConverterRoute
import dev.faizksen.kursor.settings.navigation.SettingsRoute
import dev.faizksen.kursor.watchlist.navigation.WatchlistRoute
import kotlin.reflect.KClass

enum class TopLevelDestination(
    @get:StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: KClass<*>,
) {
    RATES(
        labelRes = R.string.top_level_rates,
        icon = Icons.Default.List,
        route = WatchlistRoute::class,
    ),
    CONVERTER(
        labelRes = R.string.top_level_converter,
        icon = Icons.Default.Refresh,
        route = ConverterRoute::class,
    ),
    ALERTS(
        labelRes = R.string.top_level_alerts,
        icon = Icons.Default.Notifications,
        route = AlertsRoute::class,
    ),
    MORE(
        labelRes = R.string.top_level_more,
        icon = Icons.Default.MoreVert,
        route = SettingsRoute::class,
    )
}