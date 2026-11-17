package dev.faizksen.kursor.watchlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.faizksen.kursor.watchlist.addpair.AddPairScreen
import kotlinx.serialization.Serializable

@Serializable
data object AddPairRoute

fun NavController.navigateToAddPair() = navigate(AddPairRoute)

fun NavGraphBuilder.addPairScreen(onClose: () -> Unit) {
    composable<AddPairRoute> {
        AddPairScreen(onDismiss = onClose, onAdded = onClose)
    }
}
