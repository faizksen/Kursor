package dev.faizksen.kursor.watchlist

import android.text.format.DateFormat
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CardColumnMinWidth = 320.dp
private val CardContentSpacing = 12.dp
private val CardBorderWidth = 1.dp
private val ProgressIndicatorSize = 16.dp
private val ProgressIndicatorStroke = 2.dp
private val EmptyPlaceholderSize = 72.dp
private val EmptyContentSpacing = 12.dp
private const val MinutesInHour = 60L
private const val MinutesBeforeAbsoluteTime = 12 * MinutesInHour

@Composable
fun WatchlistScreen(
    onAddPairClick: () -> Unit,
    onPairClick: (Long) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    WatchlistContent(
        state = state,
        onAddPairClick = onAddPairClick,
        onPairClick = onPairClick,
        onAction = viewModel::obtainAction,
    )
}

@Composable
fun WatchlistContent(
    state: WatchlistUiState,
    onAddPairClick: () -> Unit,
    onPairClick: (Long) -> Unit,
    onAction: (WatchlistUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val collapsed by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }
    val results = state as? WatchlistUiState.Results

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
                            onClick = { onAction(WatchlistUiAction.MenuClicked) },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (results != null) {
                FloatingActionButton(
                    onClick = onAddPairClick,
                    shape = RoundedCornerShape(KursorRadius.button),
                    containerColor = KursorTheme.colorScheme.primary,
                    contentColor = KursorTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.watchlist_add_pair),
                        modifier = Modifier.size(KursorDimens.iconSize),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (state) {
                WatchlistUiState.Initial -> WatchlistLoading()

                WatchlistUiState.Empty -> WatchlistEmpty(onAddPairClick = onAddPairClick)

                is WatchlistUiState.Results -> WatchlistResults(
                    results = state,
                    gridState = gridState,
                    onPairClick = onPairClick,
                    onRetryClick = { onAction(WatchlistUiAction.RefreshClicked) },
                )
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
            cornerRadius = KursorRadius.listCard,
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
            )
            Text(
                text = stringResource(R.string.watchlist_empty_subtitle),
                style = KursorTheme.typography.bodyMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
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
            bottom = KursorDimens.sectionSpacing + KursorDimens.fabHeight,
        ),
        verticalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            FreshnessRow(
                lastUpdate = results.lastUpdate,
                onRetryClick = onRetryClick,
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
            horizontalArrangement = Arrangement.spacedBy(KursorDimens.bannerContentSpacing),
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
            modifier = modifier.padding(bottom = KursorDimens.sectionHeaderPaddingBottom),
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
        shape = RoundedCornerShape(KursorRadius.banner),
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
                horizontal = KursorDimens.bannerPaddingHorizontal,
                vertical = KursorDimens.bannerPaddingVertical,
            ),
            horizontalArrangement = Arrangement.spacedBy(KursorDimens.bannerContentSpacing),
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KursorRadius.listCard),
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
                        pairWithRate.watchedPair.pair.base.code,
                        pairWithRate.watchedPair.pair.quote.code,
                    ),
                    style = KursorTheme.typography.titleMedium,
                )
                Text(
                    text = rateMetaText(rate),
                    style = KursorTheme.typography.labelMedium,
                    color = KursorTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = rateValueText(rate),
                style = KursorTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun rateMetaText(rate: RatePoint?): String = if (rate == null) {
    stringResource(R.string.watchlist_rate_unavailable)
} else {
    stringResource(
        R.string.watchlist_rate_meta,
        stringResource(rate.sourceId.labelRes),
        rememberShortDateText(rate.date),
    )
}

@Composable
private fun rateValueText(rate: RatePoint?): String = if (rate == null) {
    stringResource(R.string.watchlist_rate_placeholder)
} else {
    rememberRateValueText(rate.value)
}

@Composable
private fun freshnessText(lastUpdate: LastUpdate): String = when (lastUpdate) {
    LastUpdate.Empty -> stringResource(R.string.watchlist_never_updated)

    is LastUpdate.Failed -> stringResource(failedTitleRes(lastUpdate))

    is LastUpdate.Success -> rememberUpdatedAtText(lastUpdate.updatedAt)

    is LastUpdate.Progress -> if (lastUpdate.percents > 0) {
        stringResource(R.string.watchlist_syncing_progress, lastUpdate.percents)
    } else {
        stringResource(R.string.watchlist_syncing)
    }
}

@Composable
private fun rememberUpdatedAtText(
    updatedAt: Instant,
    clock: Clock = Clock.systemDefaultZone(),
): String {
    val absoluteText = rememberShortDateTimeText(updatedAt, clock)
    val minutesAgo = remember(updatedAt, clock) {
        Duration.between(updatedAt, clock.instant()).toMinutes().coerceAtLeast(0L)
    }

    return when {
        minutesAgo < 1L -> stringResource(R.string.watchlist_updated_just_now)

        minutesAgo < MinutesInHour -> pluralStringResource(
            R.plurals.watchlist_updated_minutes_ago,
            minutesAgo.toInt(),
            minutesAgo.toInt(),
        )

        minutesAgo < MinutesBeforeAbsoluteTime -> {
            val hoursAgo = (minutesAgo / MinutesInHour).toInt()

            pluralStringResource(
                R.plurals.watchlist_updated_hours_ago,
                hoursAgo,
                hoursAgo,
            )
        }

        else -> stringResource(R.string.watchlist_updated_at, absoluteText)
    }
}

@Composable
private fun rememberShortDateText(date: LocalDate): String {
    val configuration = LocalConfiguration.current

    return remember(date, configuration) {
        val locale = configuration.locales[0]
        val pattern = DateFormat.getBestDateTimePattern(locale, "d MMM")

        DateTimeFormatter.ofPattern(pattern, locale).format(date)
    }
}

@Composable
private fun rememberRateValueText(value: BigDecimal): String {
    val configuration = LocalConfiguration.current

    return remember(value, configuration) {
        NumberFormat.getNumberInstance(configuration.locales[0]).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 4
            roundingMode = RoundingMode.HALF_UP
        }.format(value)
    }
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
): WatchedPairWithRate = WatchedPairWithRate(
    watchedPair = WatchedPair(
        id = id,
        pair = CurrencyPair(base = CurrencyCode(base), quote = CurrencyCode(quote)),
        position = id.toInt(),
    ),
    rate = value?.let {
        RatePoint(
            date = LocalDate.parse("2026-09-18"),
            value = BigDecimal(it),
            sourceId = SourceId.FRANKFURTER,
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
                lastUpdate = LastUpdate.Success(Instant.parse("2026-09-20T06:12:00Z")),
                pairs = listOf(
                    previewPair(1L, "EUR", "PLN", "4.2718"),
                    previewPair(2L, "EUR", "USD", "1.0934"),
                    previewPair(3L, "EUR", "RUB", null),
                ),
            ),
            onAddPairClick = {},
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
