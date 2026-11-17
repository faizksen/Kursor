package dev.faizksen.kursor.settings.rundetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorRadius
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.designsystem.components.ScreenTopBar
import dev.faizksen.kursor.model.AttemptResult
import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.SyncErrorKind
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.settings.R
import dev.faizksen.kursor.settings.syncstatus.rememberRunTimeText
import org.koin.compose.viewmodel.koinViewModel

private val CardBorderWidth = 1.dp
private val BadgePaddingHorizontal = 8.dp
private val BadgePaddingVertical = 3.dp
private val RowTitleSpacing = 2.dp

@Composable
fun RunDetailScreen(
    onBackClick: () -> Unit,
    viewModel: RunDetailViewModel = koinViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    RunDetailContent(state = state, onBackClick = onBackClick)
}

@Composable
internal fun RunDetailContent(
    state: RunDetailUiState,
    onBackClick: () -> Unit,
) {
    Scaffold(
        containerColor = KursorTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(
                onNavButtonClick = onBackClick,
                title = stringResource(R.string.run_detail_title),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state) {
                RunDetailUiState.Loading -> Centered { CircularProgressIndicator() }

                RunDetailUiState.Gone -> Centered {
                    Text(
                        text = stringResource(R.string.run_detail_gone),
                        style = KursorTheme.typography.bodyMedium,
                        color = KursorTheme.colorScheme.onSurfaceVariant,
                    )
                }

                is RunDetailUiState.Content -> Attempts(
                    run = state.run,
                    attempts = state.attempts,
                )
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KursorDimens.screenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun Attempts(run: SyncRun, attempts: List<ProviderAttempt>) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = KursorDimens.screenHorizontalPadding,
            end = KursorDimens.screenHorizontalPadding,
            bottom = KursorDimens.sectionSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
    ) {
        item {
            Text(
                text = runSummaryText(run),
                style = KursorTheme.typography.labelMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    top = KursorDimens.sectionHeaderPaddingBottom,
                    bottom = KursorDimens.sectionHeaderPaddingBottom,
                ),
            )
        }

        if (attempts.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.run_detail_no_attempts),
                    style = KursorTheme.typography.bodyMedium,
                    color = KursorTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(items = attempts, key = { attempt -> attempt.id }) { attempt ->
            AttemptRow(attempt = attempt)
        }
    }
}

@Composable
private fun AttemptRow(attempt: ProviderAttempt) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KursorRadius.card),
        color = KursorTheme.colorScheme.surface,
        border = BorderStroke(CardBorderWidth, KursorTheme.colorScheme.outlineVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = KursorDimens.cardPaddingHorizontal,
                vertical = KursorDimens.cardPaddingVertical,
            ),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.provider?.key
                        ?: stringResource(R.string.run_detail_without_issuer),
                    style = KursorTheme.typography.labelLarge,
                )
                Text(
                    text = attemptMetaText(attempt),
                    style = KursorTheme.typography.labelMedium,
                    color = KursorTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = RowTitleSpacing),
                )
            }
            Badge(
                text = stringResource(attempt.result.labelRes),
                containerColor = attempt.result.containerColor(),
                contentColor = attempt.result.contentColor(),
            )
        }
    }
}

@Composable
private fun Badge(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        shape = RoundedCornerShape(KursorRadius.deltaBadge),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            style = KursorTheme.typography.labelSmall,
            modifier = Modifier.padding(
                horizontal = BadgePaddingHorizontal,
                vertical = BadgePaddingVertical,
            ),
        )
    }
}

@Composable
private fun runSummaryText(run: SyncRun): String = stringResource(
    R.string.run_detail_summary,
    run.updatedCount ?: 0,
    run.requestedCount ?: 0,
    rememberRunTimeText(run.finishedAt),
)

@Composable
private fun attemptMetaText(attempt: ProviderAttempt): String {
    val pairs = stringResource(
        R.string.run_detail_pairs,
        attempt.coveredCount,
        attempt.requestedCount,
    )

    return attempt.errorKind?.let { kind ->
        stringResource(R.string.run_detail_pairs_with_reason, pairs, stringResource(kind.labelRes))
    } ?: pairs
}

@Composable
private fun AttemptResult.containerColor(): Color = when (this) {
    AttemptResult.Answered -> KursorTheme.colors.rateUpContainer
    AttemptResult.Empty -> KursorTheme.colors.staleContainer
    AttemptResult.Failed -> KursorTheme.colorScheme.errorContainer
}

@Composable
private fun AttemptResult.contentColor(): Color = when (this) {
    AttemptResult.Answered -> KursorTheme.colors.rateUp
    AttemptResult.Empty -> KursorTheme.colors.stale
    AttemptResult.Failed -> KursorTheme.colorScheme.onErrorContainer
}

private val AttemptResult.labelRes: Int
    get() = when (this) {
        AttemptResult.Answered -> R.string.run_detail_answered
        AttemptResult.Empty -> R.string.run_detail_empty
        AttemptResult.Failed -> R.string.run_detail_failed
    }

private val SyncErrorKind.labelRes: Int
    get() = when (this) {
        SyncErrorKind.NoNetwork -> R.string.sync_error_no_network
        SyncErrorKind.Timeout -> R.string.sync_error_timeout
        SyncErrorKind.ServerError -> R.string.sync_error_server
        SyncErrorKind.RateLimited -> R.string.sync_error_rate_limited
        SyncErrorKind.InvalidCurrency -> R.string.sync_error_invalid_currency
        SyncErrorKind.ParseError -> R.string.sync_error_parse
        SyncErrorKind.UnknownSource -> R.string.sync_error_unknown_source
        SyncErrorKind.Unknown -> R.string.sync_error_unknown
    }
