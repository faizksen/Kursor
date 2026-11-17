package dev.faizksen.kursor.watchlist.addpair

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun AddPairEffects(
    effects: Flow<AddPairUiEffect>,
    onDismiss: () -> Unit,
    onAdded: () -> Unit,
) {
    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                AddPairUiEffect.Added -> onAdded()
                AddPairUiEffect.AlreadyWatched -> onAdded()
                AddPairUiEffect.Dismissed -> onDismiss()
            }
        }
    }
}
