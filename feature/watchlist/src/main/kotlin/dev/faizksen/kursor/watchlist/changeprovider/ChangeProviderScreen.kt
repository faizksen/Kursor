package dev.faizksen.kursor.watchlist.changeprovider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.designsystem.components.KursorButton
import dev.faizksen.kursor.designsystem.components.ScreenTopBar
import dev.faizksen.kursor.watchlist.R
import dev.faizksen.kursor.watchlist.addpair.ProviderList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChangeProviderScreen(
    onClose: () -> Unit,
    viewModel: ChangeProviderViewModel = koinViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    ChangeProviderEffects(effects = viewModel.effect, onClose = onClose)

    ChangeProviderContent(state = state, onAction = viewModel::obtainAction, onClose = onClose)
}

@Composable
internal fun ChangeProviderContent(
    state: ChangeProviderUiState,
    onAction: (ChangeProviderUiAction) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        containerColor = KursorTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(
                onNavButtonClick = onClose,
                title = stringResource(R.string.change_provider_title),
            )
        },
        bottomBar = {
            if (state is ChangeProviderUiState.Picking) {
                Surface(color = KursorTheme.colorScheme.surfaceVariant) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = KursorDimens.screenHorizontalPadding,
                                vertical = KursorDimens.cardPaddingVertical,
                            ),
                    ) {
                        KursorButton(
                            text = stringResource(R.string.change_provider_confirm),
                            onClick = { onAction(ChangeProviderUiAction.ConfirmClicked) },
                            enabled = state.selected != null && state.selected != state.current,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state) {
                ChangeProviderUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                ChangeProviderUiState.Gone -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = KursorDimens.screenHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.change_provider_gone),
                        style = KursorTheme.typography.bodyMedium,
                        color = KursorTheme.colorScheme.onSurfaceVariant,
                    )
                }

                is ChangeProviderUiState.Picking -> Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(
                            R.string.change_provider_subtitle,
                            state.pair.base.code,
                            state.pair.quote.code,
                        ),
                        style = KursorTheme.typography.titleMedium,
                        modifier = Modifier.padding(
                            start = KursorDimens.screenHorizontalPadding,
                            end = KursorDimens.screenHorizontalPadding,
                            bottom = KursorDimens.sectionSpacing,
                        ),
                    )

                    ProviderList(
                        providers = state.providers,
                        selected = state.selected,
                        everyProviderCount = state.everyProviderCount,
                        showingEveryProvider = state.showingEveryProvider,
                        onPick = { key -> onAction(ChangeProviderUiAction.ProviderPicked(key)) },
                        onShowEvery = {
                            onAction(ChangeProviderUiAction.ShowEveryProviderClicked)
                        },
                    )
                }
            }
        }
    }
}
