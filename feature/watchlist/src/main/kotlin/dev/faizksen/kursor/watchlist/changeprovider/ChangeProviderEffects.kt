package dev.faizksen.kursor.watchlist.changeprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun ChangeProviderEffects(
    effects: Flow<ChangeProviderUiEffect>,
    onClose: () -> Unit,
) {
    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                ChangeProviderUiEffect.Changed -> onClose()
            }
        }
    }
}
