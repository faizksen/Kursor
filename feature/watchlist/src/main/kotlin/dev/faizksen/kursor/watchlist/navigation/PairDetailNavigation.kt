package dev.faizksen.kursor.watchlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.faizksen.kursor.watchlist.pairdetail.PairDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data class PairDetailRoute(val pairId: Long)

fun NavController.navigateToPairDetail(pairId: Long) = navigate(PairDetailRoute(pairId))

fun NavGraphBuilder.pairDetailScreen(
    onBackClick: () -> Unit,
    onChangeProviderClick: (Long) -> Unit,
    onDeleted: () -> Unit,
) {
    composable<PairDetailRoute> { entry ->
        val pairId = entry.toRoute<PairDetailRoute>().pairId

        PairDetailScreen(
            onBackClick = onBackClick,
            onChangeProviderClick = { onChangeProviderClick(pairId) },
            onDeleted = onDeleted,
        )
    }
}
