package dev.faizksen.kursor.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.faizksen.kursor.settings.syncstatus.SyncStatusScreen
import kotlinx.serialization.Serializable

@Serializable
data object SyncStatusRoute

fun NavController.navigateToSyncStatus() = navigate(SyncStatusRoute)

fun NavGraphBuilder.syncStatusScreen(onBackClick: () -> Unit) {
    composable<SyncStatusRoute> {
        SyncStatusScreen(onBackClick = onBackClick)
    }
}
