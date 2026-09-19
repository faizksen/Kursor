package dev.faizksen.kursor.watchlist

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onAddPairClick: () -> Unit,
    onPairClick: (Long) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    WatchlistContent(
        state = state,
        onAddPairClick = onAddPairClick,
        onPairClick = onPairClick,
        onAction = { viewModel.obtainAction(it) }
    )
}

@ExperimentalMaterial3Api
@Composable
fun WatchlistContent(
    state: WatchlistUiState,
    onAddPairClick: () -> Unit,
    onPairClick: (Long) -> Unit,
    onAction: (WatchlistUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        color = KursorTheme.colorScheme.onSurface,
                        style = KursorTheme.typography.titleMedium,
                        text = stringResource(R.string.watchlist_title)
                    )
                },
                actions = {
                    if (state is WatchlistUiState.Results) {
                        IconButton(onClick = { onAction(WatchlistUiAction.RefreshClicked) }) {
                            Surface(
                                shape = CircleShape,
                                color = KursorTheme.colorScheme.surface,
                                shadowElevation = 6.dp,
                                modifier = Modifier.size(KursorDimens.iconButtonSize),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Обновить",
                                    tint = KursorTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }
                }
            )
        },
    ) { _ ->
        when (state) {
            WatchlistUiState.Initial -> CircularProgressIndicator()
            WatchlistUiState.Empty -> Text(stringResource(R.string.watchlist_empty_title))
            is WatchlistUiState.Results -> Text("Пар: ${state.pairs.size}")
        }
    }
}