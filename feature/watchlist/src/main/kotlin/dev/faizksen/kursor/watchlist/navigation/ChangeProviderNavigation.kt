package dev.faizksen.kursor.watchlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.faizksen.kursor.watchlist.changeprovider.ChangeProviderScreen
import kotlinx.serialization.Serializable

@Serializable
data class ChangeProviderRoute(val pairId: Long)

fun NavController.navigateToChangeProvider(pairId: Long) = navigate(ChangeProviderRoute(pairId))

fun NavGraphBuilder.changeProviderScreen(onClose: () -> Unit) {
    composable<ChangeProviderRoute> {
        ChangeProviderScreen(onClose = onClose)
    }
}
