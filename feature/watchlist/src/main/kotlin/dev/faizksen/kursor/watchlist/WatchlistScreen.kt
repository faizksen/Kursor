package dev.faizksen.kursor.watchlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorRadius
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.designsystem.components.DashedPlaceholder
import dev.faizksen.kursor.designsystem.components.KursorButton
import dev.faizksen.kursor.designsystem.components.KursorSmallButton
import dev.faizksen.kursor.designsystem.components.RootTopBar
import dev.faizksen.kursor.designsystem.components.TopAppBarButton
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

private const val SnackbarFitsBesideFabUpTo = 1f

private val CardColumnMinWidth = 320.dp
private val FabClearance = KursorDimens.fabHeight + KursorDimens.sectionSpacing * 2
private val CardContentSpacing = 12.dp
private val CardBorderWidth = 1.dp
private val ProgressIndicatorSize = 16.dp
private val ProgressIndicatorStroke = 2.dp
private val EmptyPlaceholderSize = 72.dp
private val EmptyContentSpacing = 12.dp

@Composable
fun WatchlistScreen(
    onAddPairClick: () -> Unit,
    onPairClick: (Long) -> Unit,
    onSyncStatusClick: () -> Unit,
    onRunClick: (Long) -> Unit,
    viewModel: WatchlistViewModel = koinViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val deletedPair by viewModel.deletedPair.collectAsStateWithLifecycle()

    WatchlistContent(
        state = state,
        deletedPair = deletedPair,
        onAddPairClick = onAddPairClick,
        onPairClick = onPairClick,
        onSyncStatusClick = onSyncStatusClick,
        onRunClick = onRunClick,
        onAction = viewModel::obtainAction,
    )
}

@Composable
fun WatchlistContent(
    state: WatchlistUiState,
    deletedPair: CurrencyPair? = null,
    onAddPairClick: () -> Unit,
    onPairClick: (Long) -> Unit,
    onSyncStatusClick: () -> Unit,
    onRunClick: (Long) -> Unit = {},
    onAction: (WatchlistUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val collapsed by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }
    val results = state as? WatchlistUiState.Results
    val snackbarHostState = remember { SnackbarHostState() }

    DeletedPairSnackbar(
        pair = deletedPair,
        hostState = snackbarHostState,
        onUndo = { onAction(WatchlistUiAction.UndoDeleteClicked) },
        onDismiss = { onAction(WatchlistUiAction.DeleteNoticeDismissed) },
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
        containerColor = KursorTheme.colorScheme.background,
        topBar = {
            RootTopBar(
                title = stringResource(R.string.watchlist_title),
                lifted = gridState.canScrollBackward,
                collapsed = collapsed,
                compactContent = {
                    if (results != null) {
                        Text(
                            text = freshnessText(results.lastUpdate),
                            style = KursorTheme.typography.labelMedium,
                            color = KursorTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = KursorDimens.screenHorizontalPadding),
                        )
                    }
                },
                actions = {
                    if (results != null) {
                        TopAppBarButton(
                            icon = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.watchlist_action_refresh),
                            onClick = { onAction(WatchlistUiAction.RefreshClicked) },
                            enabled = results.lastUpdate !is LastUpdate.Progress,
                        )
                        TopAppBarButton(
                            icon = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.watchlist_action_more),
                            onClick = onSyncStatusClick,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (state) {
                WatchlistUiState.Loading -> WatchlistLoading()

                WatchlistUiState.Empty -> WatchlistEmpty(onAddPairClick = onAddPairClick)

                is WatchlistUiState.Results -> WatchlistResults(
                    results = state,
                    gridState = gridState,
                    onPairClick = onPairClick,
                    onRetryClick = { onAction(WatchlistUiAction.RefreshClicked) },
                    onRunClick = onRunClick,
                )
            }

            BottomBar(
                snackbarHostState = snackbarHostState,
                fabVisible = results != null && rememberScrollingUp(gridState),
                onAddPairClick = onAddPairClick,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

@Composable
private fun BottomBar(
    snackbarHostState: SnackbarHostState,
    fabVisible: Boolean,
    onAddPairClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cramped = LocalDensity.current.fontScale > SnackbarFitsBesideFabUpTo

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(KursorDimens.sectionSpacing),
        verticalArrangement = Arrangement.spacedBy(KursorDimens.sectionSpacing),
        horizontalAlignment = Alignment.End,
    ) {
        if (cramped) {
            SnackbarHost(hostState = snackbarHostState)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = KursorDimens.sectionSpacing,
                alignment = Alignment.End,
            ),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (!cramped) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }

            AnimatedVisibility(visible = fabVisible, enter = fadeIn(), exit = fadeOut()) {
                AddPairFab(onClick = onAddPairClick)
            }
        }
    }
}

@Composable
private fun WatchlistLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = KursorTheme.colorScheme.primary)
    }
}

@Composable
private fun WatchlistEmpty(
    onAddPairClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = KursorDimens.screenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DashedPlaceholder(
            size = EmptyPlaceholderSize,
            cornerRadius = KursorRadius.card,
        )
        Column(
            modifier = Modifier.padding(vertical = KursorDimens.sectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EmptyContentSpacing),
        ) {
            Text(
                text = stringResource(R.string.watchlist_empty_title),
                style = KursorTheme.typography.titleMedium,
                color = KursorTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.watchlist_empty_subtitle),
                style = KursorTheme.typography.bodyMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        KursorButton(
            text = stringResource(R.string.watchlist_add_pair),
            onClick = onAddPairClick,
        )
    }
}

@Composable
private fun WatchlistResults(
    results: WatchlistUiState.Results,
    onPairClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
    onRunClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = CardColumnMinWidth),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = KursorDimens.screenHorizontalPadding,
            end = KursorDimens.screenHorizontalPadding,
            top = KursorDimens.sectionHeaderPaddingBottom,
            bottom = FabClearance,
        ),
        verticalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            FreshnessRow(
                lastUpdate = results.lastUpdate,
                onRetryClick = onRetryClick,
                onRunClick = onRunClick,
            )
        }
        items(items = results.pairs, key = { it.watchedPair.id }) { pairWithRate ->
            RateCard(
                pairWithRate = pairWithRate,
                onClick = { onPairClick(pairWithRate.watchedPair.id) },
            )
        }
    }
}

@Composable
private fun FreshnessRow(
    lastUpdate: LastUpdate,
    onRetryClick: () -> Unit,
    onRunClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (lastUpdate) {
        is LastUpdate.Failed -> FreshnessRowFailed(
            lastUpdate = lastUpdate,
            onTryAgainClick = onRetryClick,
            modifier = modifier,
        )

        is LastUpdate.Progress -> Row(
            modifier = modifier.padding(bottom = KursorDimens.sectionHeaderPaddingBottom),
            horizontalArrangement = Arrangement.spacedBy(KursorDimens.cardContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(ProgressIndicatorSize),
                color = KursorTheme.colorScheme.primary,
                strokeWidth = ProgressIndicatorStroke,
            )
            Text(
                text = freshnessText(lastUpdate),
                style = KursorTheme.typography.labelMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
            )
        }

        LastUpdate.Empty,
        is LastUpdate.Success,
        -> Text(
            text = freshnessText(lastUpdate),
            style = KursorTheme.typography.labelMedium,
            color = KursorTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
                .let { base ->
                    val partial = (lastUpdate as? LastUpdate.Success)?.partialCount()

                    if (partial == null) base else base.clickable {
                        onRunClick(lastUpdate.runId)
                    }
                }
                .padding(bottom = KursorDimens.sectionHeaderPaddingBottom),
        )
    }
}

private fun failedTitleRes(lastUpdate: LastUpdate.Failed): Int = if (lastUpdate.willRetry) {
    R.string.watchlist_sync_will_retry
} else {
    R.string.watchlist_sync_failed
}

@Composable
private fun FreshnessRowFailed(
    lastUpdate: LastUpdate.Failed,
    onTryAgainClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastTryText = rememberShortDateTimeText(lastUpdate.lastTryAt)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KursorRadius.cardCompact),
        color = if (lastUpdate.willRetry) {
            KursorTheme.colorScheme.tertiaryContainer
        } else {
            KursorTheme.colorScheme.errorContainer
        },
        contentColor = if (lastUpdate.willRetry) {
            KursorTheme.colorScheme.onTertiaryContainer
        } else {
            KursorTheme.colorScheme.onErrorContainer
        },
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = KursorDimens.cardCompactPaddingHorizontal,
                vertical = KursorDimens.cardCompactPaddingVertical,
            ),
            horizontalArrangement = Arrangement.spacedBy(KursorDimens.cardContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(failedTitleRes(lastUpdate)),
                    style = KursorTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.watchlist_sync_failed_attempts,
                        lastUpdate.failedAttempts,
                        lastUpdate.failedAttempts,
                        lastTryText,
                    ),
                    style = KursorTheme.typography.labelMedium,
                )
            }
            KursorSmallButton(
                text = stringResource(R.string.watchlist_retry),
                onClick = onTryAgainClick,
                containerColor = if (lastUpdate.willRetry) {
                    KursorTheme.colorScheme.tertiary
                } else {
                    KursorTheme.colorScheme.error
                },
                contentColor = if (lastUpdate.willRetry) {
                    KursorTheme.colorScheme.onTertiary
                } else {
                    KursorTheme.colorScheme.onError
                },
            )
        }
    }
}

@Composable
private fun RateCard(
    pairWithRate: WatchedPairWithRate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rate = pairWithRate.rate
    val shown = pairWithRate.watchedPair.displayedPair()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KursorRadius.card),
        color = KursorTheme.colorScheme.surface,
        contentColor = KursorTheme.colorScheme.onSurface,
        border = BorderStroke(CardBorderWidth, KursorTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(
                    horizontal = KursorDimens.cardPaddingHorizontal,
                    vertical = KursorDimens.cardPaddingVertical,
                ),
            horizontalArrangement = Arrangement.spacedBy(CardContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        R.string.watchlist_pair,
                        shown.base.code,
                        shown.quote.code,
                    ),
                    style = KursorTheme.typography.titleMedium,
                )
                Text(
                    text = rateMetaText(
                        provider = pairWithRate.watchedPair.provider,
                        rate = rate,
                        quiet = pairWithRate.quiet,
                    ),
                    style = KursorTheme.typography.labelMedium,
                    color = if (pairWithRate.quiet) {
                        KursorTheme.colors.stale
                    } else {
                        KursorTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Text(
                text = rateValueText(pairWithRate.displayedValue()),
                style = KursorTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun rateMetaText(
    provider: ProviderKey?,
    rate: RatePoint?,
    quiet: Boolean,
): String {
    val issuer = provider?.key ?: rate?.let { stringResource(it.sourceId.labelRes) }

    return when {
        issuer == null -> stringResource(R.string.watchlist_rate_unavailable)
        quiet -> stringResource(R.string.watchlist_rate_quiet, issuer)
        rate == null -> issuer
        else -> stringResource(
            R.string.watchlist_rate_meta,
            issuer,
            rememberShortDateText(rate.date),
        )
    }
}

@Composable
private fun rateValueText(value: Rate?): String = if (value == null) {
    stringResource(R.string.watchlist_rate_placeholder)
} else {
    rememberRateValueText(value)
}

@Composable
private fun freshnessText(lastUpdate: LastUpdate): String = when (lastUpdate) {
    LastUpdate.Empty -> stringResource(R.string.watchlist_never_updated)

    is LastUpdate.Failed -> stringResource(failedTitleRes(lastUpdate))

    is LastUpdate.Success -> {
        val updatedAt = rememberUpdatedAtText(lastUpdate.updatedAt)
        val partial = lastUpdate.partialCount()

        if (partial == null) {
            updatedAt
        } else {
            stringResource(R.string.watchlist_updated_partially, partial, updatedAt)
        }
    }

    is LastUpdate.Progress -> if (lastUpdate.percents > 0) {
        stringResource(R.string.watchlist_syncing_progress, lastUpdate.percents)
    } else {
        stringResource(R.string.watchlist_syncing)
    }
}

@Composable
private fun rememberUpdatedAtText(
    updatedAt: Instant,
    clock: Clock = Clock.System,
): String = when (val freshness = rememberFreshness(updatedAt, clock)) {
    Freshness.JustNow -> stringResource(R.string.watchlist_updated_just_now)

    is Freshness.Minutes -> pluralStringResource(
        R.plurals.watchlist_updated_minutes_ago,
        freshness.value,
        freshness.value,
    )

    is Freshness.Hours -> pluralStringResource(
        R.plurals.watchlist_updated_hours_ago,
        freshness.value,
        freshness.value,
    )

    is Freshness.At -> stringResource(
        R.string.watchlist_updated_at,
        rememberShortDateTimeText(freshness.instant, clock),
    )
}

@Composable
private fun rememberFreshness(updatedAt: Instant, clock: Clock): Freshness {
    var freshness by remember(updatedAt, clock) {
        mutableStateOf(freshnessOf(updatedAt, clock.now()))
    }

    LaunchedEffect(updatedAt, clock) {
        var delayUntilChange = delayUntilFreshnessChanges(updatedAt, clock.now())

        while (delayUntilChange != null) {
            delay(delayUntilChange)
            freshness = freshnessOf(updatedAt, clock.now())
            delayUntilChange = delayUntilFreshnessChanges(updatedAt, clock.now())
        }
    }

    return freshness
}

private val SourceId.labelRes: Int
    get() = when (this) {
        SourceId.FRANKFURTER -> R.string.source_frankfurter
        SourceId.COINGECKO -> R.string.source_coingecko
        SourceId.CBR -> R.string.source_cbr
    }

private fun previewPair(
    id: Long,
    base: String,
    quote: String,
    value: String?,
    provider: String = "ECB",
): WatchedPairWithRate = WatchedPairWithRate(
    watchedPair = WatchedPair(
        id = id,
        pair = CurrencyPair(base = CurrencyCode(base), quote = CurrencyCode(quote)),
        provider = ProviderKey(provider),
        position = id.toInt(),
        addedAt = Instant.parse("2026-09-18T00:00:00Z"),
    ),
    rate = value?.let {
        RatePoint(
            date = LocalDate.parse("2026-09-18"),
            value = Rate.parse(it)!!,
            sourceId = SourceId.FRANKFURTER,
            provider = ProviderKey(provider),
        )
    },
)

@PreviewLightDark
@Preview(locale = "ru", showBackground = true, backgroundColor = 0xFFF7F6F3)
@Composable
private fun WatchlistResultsPreview() {
    KursorTheme {
        WatchlistContent(
            state = WatchlistUiState.Results(
                lastUpdate = LastUpdate.Success(
                    updatedAt = Instant.parse("2026-09-20T06:12:00Z"),
                    updatedCount = 3,
                    requestedCount = 4,
                ),
                pairs = listOf(
                    previewPair(1L, "EUR", "PLN", "4.2718"),
                    previewPair(2L, "EUR", "USD", "1.0934"),
                    previewPair(3L, "EUR", "RUB", null),
                ),
            ),
            onAddPairClick = {},
            onSyncStatusClick = {},
            onPairClick = {},
            onAction = {},
        )
    }
}

@PreviewLightDark
@Preview(locale = "ru", showBackground = true, backgroundColor = 0xFFF7F6F3)
@Composable
private fun WatchlistEmptyPreview() {
    KursorTheme {
        WatchlistContent(
            state = WatchlistUiState.Empty,
            onAddPairClick = {},
            onSyncStatusClick = {},
            onPairClick = {},
            onAction = {},
        )
    }
}

@PreviewLightDark
@Preview(locale = "ru", showBackground = true, backgroundColor = 0xFFF7F6F3)
@Composable
private fun FreshnessRowFailedPreview() {
    KursorTheme {
        FreshnessRowFailed(
            lastUpdate = LastUpdate.Failed(
                failedAttempts = 5,
                lastTryAt = Instant.parse("2026-09-20T06:12:00Z"),
                willRetry = false,
            ),
            onTryAgainClick = {},
        )
    }
}
