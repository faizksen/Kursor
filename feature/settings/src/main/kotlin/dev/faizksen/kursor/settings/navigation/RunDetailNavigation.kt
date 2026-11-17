package dev.faizksen.kursor.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.faizksen.kursor.settings.rundetail.RunDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data class RunDetailRoute(val runId: Long)

fun NavController.navigateToRunDetail(runId: Long) = navigate(RunDetailRoute(runId))

fun NavGraphBuilder.runDetailScreen(onBackClick: () -> Unit) {
    composable<RunDetailRoute> {
        RunDetailScreen(onBackClick = onBackClick)
    }
}
