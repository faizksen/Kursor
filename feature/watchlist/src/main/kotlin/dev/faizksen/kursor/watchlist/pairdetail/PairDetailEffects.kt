package dev.faizksen.kursor.watchlist.pairdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun PairDetailEffects(effects: Flow<PairDetailUiEffect>, onDeleted: () -> Unit) {
    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                PairDetailUiEffect.Deleted -> onDeleted()
            }
        }
    }
}
