package dev.faizksen.kursor.watchlist

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import dev.faizksen.kursor.model.CurrencyPair

@Composable
internal fun DeletedPairSnackbar(
    pair: CurrencyPair?,
    hostState: SnackbarHostState,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (pair == null) return

    val message = stringResource(R.string.watchlist_pair_deleted, pair.base.code, pair.quote.code)
    val undo = stringResource(R.string.watchlist_undo)

    LaunchedEffect(pair) {
        val result = hostState.showSnackbar(
            message = message,
            actionLabel = undo,
            withDismissAction = false,
            duration = SnackbarDuration.Long,
        )

        when (result) {
            SnackbarResult.ActionPerformed -> onUndo()
            SnackbarResult.Dismissed -> onDismiss()
        }
    }
}
