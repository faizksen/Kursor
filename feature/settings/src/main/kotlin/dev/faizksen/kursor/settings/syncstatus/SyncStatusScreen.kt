package dev.faizksen.kursor.settings.syncstatus

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorRadius
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.designsystem.components.KursorButton
import dev.faizksen.kursor.designsystem.components.KursorSmallButton
import dev.faizksen.kursor.designsystem.components.ScreenTopBar
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncErrorKind
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.settings.R
import java.text.NumberFormat
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

private val TwoPaneMaxHeight = 480.dp
private val PaneSpacing = 10.dp
private val BadgePaddingHorizontal = 8.dp
private val BadgePaddingVertical = 2.dp
private val CardBorderWidth = 1.dp
private val CardContentSpacing = 12.dp
private val BottomBarPaddingTop = 12.dp

private const val ActivePaneWeight = 0.44f
private const val HistoryPaneWeight = 0.56f
private const val MillisInSecond = 1000L
private const val SecondsFractionDigits = 1

private const val ActiveHeaderKey = "active_header"
private const val HistoryHeaderKey = "history_header"
private const val HistoryEmptyKey = "history_empty"

@Composable
fun SyncStatusScreen(
    onBackClick: () -> Unit,
    viewModel: SyncStatusViewModel = hiltViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    SyncStatusContent(
        state = state,
        onBackClick = onBackClick,
        onAction = viewModel::obtainAction,
    )
}

@Composable
fun SyncStatusContent(
    state: SyncStatusUiState,
    onBackClick: () -> Unit,
    onAction: (SyncStatusUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val singleColumnState = rememberLazyListState()
    val activeWorkState = rememberLazyListState()
    val historyState = rememberLazyListState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val twoPane = maxHeight < TwoPaneMaxHeight
        val content = state as? SyncStatusUiState.Content
        val lifted = if (twoPane) {
            activeWorkState.canScrollBackward || historyState.canScrollBackward
        } else {
            singleColumnState.canScrollBackward
        }

        Scaffold(
            containerColor = KursorTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            topBar = {
                ScreenTopBar(
                    navButtonIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNavButtonClick = onBackClick,
                    title = stringResource(R.string.sync_status_title),
                    lifted = lifted,
                    actions = {
                        if (twoPane && content != null) {
                            RunSyncAction(
                                enabled = !content.isSyncing,
                                onClick = { onAction(SyncStatusUiAction.RunSyncClicked) },
                            )
                        }
                    },
                )
            },
            bottomBar = {
                if (!twoPane && content != null) {
                    RunSyncBar(
                        enabled = !content.isSyncing,
                        onClick = { onAction(SyncStatusUiAction.RunSyncClicked) },
                    )
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (state) {
                    SyncStatusUiState.Loading -> SyncStatusLoading()

                    is SyncStatusUiState.Content -> if (twoPane) {
                        SyncStatusPanes(
                            content = state,
                            activeWorkState = activeWorkState,
                            historyState = historyState,
                        )
                    } else {
                        SyncStatusSingleColumn(
                            content = state,
                            listState = singleColumnState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusSingleColumn(
    content: SyncStatusUiState.Content,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = KursorDimens.screenHorizontalPadding,
            end = KursorDimens.screenHorizontalPadding,
            bottom = KursorDimens.sectionSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
    ) {
        activeWorkItems(periodic = content.periodic, manual = content.manual, compact = false)
        runItems(runs = content.runs, compact = false)
    }
}

@Composable
private fun SyncStatusPanes(
    content: SyncStatusUiState.Content,
    activeWorkState: LazyListState,
    historyState: LazyListState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = KursorDimens.screenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(PaneSpacing),
    ) {
        LazyColumn(
            state = activeWorkState,
            modifier = Modifier
                .weight(ActivePaneWeight)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = KursorDimens.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
        ) {
            activeWorkItems(periodic = content.periodic, manual = content.manual, compact = true)
        }
        LazyColumn(
            state = historyState,
            modifier = Modifier
                .weight(HistoryPaneWeight)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = KursorDimens.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
        ) {
            runItems(runs = content.runs, compact = true)
        }
    }
}

private fun LazyListScope.activeWorkItems(
    periodic: SyncQueue,
    manual: SyncQueue,
    compact: Boolean,
) {
    item(key = ActiveHeaderKey) {
        SectionHeader(text = stringResource(R.string.sync_status_now_title))
    }
    item(key = SyncTrigger.Periodic.name) {
        WorkRow(trigger = SyncTrigger.Periodic, queue = periodic, compact = compact)
    }
    item(key = SyncTrigger.Manual.name) {
        WorkRow(trigger = SyncTrigger.Manual, queue = manual, compact = compact)
    }
}

private fun LazyListScope.runItems(
    runs: List<SyncRun>,
    compact: Boolean,
) {
    item(key = HistoryHeaderKey) {
        SectionHeader(text = stringResource(R.string.sync_status_history_title))
    }
    if (runs.isEmpty()) {
        item(key = HistoryEmptyKey) {
            Text(
                text = stringResource(R.string.sync_status_history_empty),
                style = KursorTheme.typography.bodyMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        items(items = runs, key = { run -> run.id }) { run ->
            RunRow(run = run, compact = compact)
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = KursorTheme.typography.titleMedium,
        modifier = modifier.padding(
            top = KursorDimens.sectionHeaderPaddingTop,
            bottom = KursorDimens.sectionHeaderPaddingBottom,
        ),
    )
}

@Composable
private fun WorkRow(
    trigger: SyncTrigger,
    queue: SyncQueue,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    StatusCard(compact = compact, modifier = modifier) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(trigger.labelRes),
                style = KursorTheme.typography.bodyMedium,
            )
            val schedule = scheduleText(queue)
            if (schedule != null) {
                Text(
                    text = schedule,
                    style = KursorTheme.typography.labelMedium,
                    color = KursorTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        StatusBadge(
            text = stringResource(queue.state.labelRes),
            containerColor = queue.state.badgeContainerColor(),
            contentColor = queue.state.badgeContentColor(),
        )
    }
}

@Composable
private fun scheduleText(queue: SyncQueue): String? {
    val nextRunAt = queue.nextRunAt ?: return null
    val waitingForRetry = queue.isWaitingForRetry

    return when (val countdown = rememberCountdown(nextRunAt)) {
        Countdown.Imminent -> stringResource(
            if (waitingForRetry) {
                R.string.sync_status_retry_awaiting
            } else {
                R.string.sync_status_next_run_awaiting
            }
        )

        is Countdown.Minutes -> scheduleInText(
            waitingForRetry = waitingForRetry,
            countdown = stringResource(
                R.string.sync_status_countdown_minutes,
                countdown.value,
            ),
        )

        is Countdown.Hours -> scheduleInText(
            waitingForRetry = waitingForRetry,
            countdown = stringResource(
                R.string.sync_status_countdown_hours,
                countdown.value,
                countdown.minutes,
            ),
        )
    }
}

@Composable
private fun scheduleInText(waitingForRetry: Boolean, countdown: String): String = stringResource(
    if (waitingForRetry) {
        R.string.sync_status_retry_in
    } else {
        R.string.sync_status_next_run_in
    },
    countdown,
)

@Composable
private fun rememberCountdown(
    target: Instant,
    clock: Clock = Clock.systemDefaultZone(),
): Countdown {
    var countdown by remember(target, clock) {
        mutableStateOf(countdownTo(target, clock.instant()))
    }

    LaunchedEffect(target, clock) {
        var delayUntilChange = delayUntilCountdownChanges(target, clock.instant())

        while (delayUntilChange != null) {
            delay(delayUntilChange.toMillis())
            countdown = countdownTo(target, clock.instant())
            delayUntilChange = delayUntilCountdownChanges(target, clock.instant())
        }
    }

    return countdown
}

@Composable
private fun RunRow(
    run: SyncRun,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    StatusCard(compact = compact, modifier = modifier) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = runTitleText(run),
                style = KursorTheme.typography.labelLarge,
            )
            Text(
                text = runMetaText(run),
                style = KursorTheme.typography.labelMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
            )
        }
        StatusBadge(
            text = stringResource(run.result.labelRes),
            containerColor = run.result.badgeContainerColor(),
            contentColor = run.result.badgeContentColor(),
        )
    }
}

@Composable
private fun StatusCard(
    compact: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) KursorRadius.cardCompact else KursorRadius.card),
        color = KursorTheme.colorScheme.surface,
        contentColor = KursorTheme.colorScheme.onSurface,
        border = BorderStroke(CardBorderWidth, KursorTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) {
                    KursorDimens.cardCompactPaddingHorizontal
                } else {
                    KursorDimens.cardPaddingHorizontal
                },
                vertical = if (compact) {
                    KursorDimens.cardCompactPaddingVertical
                } else {
                    KursorDimens.cardPaddingVertical
                },
            ),
            horizontalArrangement = Arrangement.spacedBy(CardContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(KursorRadius.deltaBadge),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            style = KursorTheme.typography.labelMedium,
            modifier = Modifier.padding(
                horizontal = BadgePaddingHorizontal,
                vertical = BadgePaddingVertical,
            ),
        )
    }
}

@Composable
private fun RunSyncAction(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KursorSmallButton(
        text = stringResource(R.string.sync_status_run_now),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.padding(end = KursorDimens.screenHorizontalPadding),
    )
}

@Composable
private fun RunSyncBar(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = KursorTheme.colorScheme.surface,
    ) {
        Column {
            HorizontalDivider(
                thickness = CardBorderWidth,
                color = KursorTheme.colorScheme.outlineVariant,
            )
            KursorButton(
                text = stringResource(R.string.sync_status_run_now),
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = KursorDimens.screenHorizontalPadding,
                        end = KursorDimens.screenHorizontalPadding,
                        top = BottomBarPaddingTop,
                        bottom = KursorDimens.sectionSpacing,
                    ),
            )
        }
    }
}

@Composable
private fun SyncStatusLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = KursorTheme.colorScheme.primary)
    }
}

@Composable
private fun runTitleText(run: SyncRun): String = stringResource(
    R.string.sync_status_meta_separator,
    stringResource(run.sourceId.labelRes),
    rememberRunTimeText(run.finishedAt),
)

@Composable
private fun runMetaText(run: SyncRun): String = stringResource(
    R.string.sync_status_meta_separator,
    runDetailText(run),
    rememberDurationText(run.startedAt, run.finishedAt),
)

@Composable
private fun runDetailText(run: SyncRun): String {
    val errorKind = run.errorKind
    val updatedCount = run.updatedCount

    return when {
        errorKind != null -> stringResource(
            R.string.sync_status_meta_separator,
            stringResource(R.string.sync_status_attempt, run.attempt),
            stringResource(errorKind.labelRes),
        )

        updatedCount != null -> pluralStringResource(
            R.plurals.sync_status_updated_count,
            updatedCount,
            updatedCount,
        )

        else -> stringResource(R.string.sync_status_attempt, run.attempt)
    }
}

@Composable
private fun rememberRunTimeText(
    instant: Instant,
    clock: Clock = Clock.systemDefaultZone(),
): String {
    val configuration = LocalConfiguration.current

    return remember(instant, configuration, clock) {
        val locale = configuration.locales[0]
        val finishedOn = instant.atZone(clock.zone).toLocalDate()
        val skeleton = if (finishedOn == LocalDate.now(clock)) TimeSkeleton else DateTimeSkeleton
        val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)

        DateTimeFormatter.ofPattern(pattern, locale).withZone(clock.zone).format(instant)
    }
}

@Composable
private fun rememberDurationText(startedAt: Instant, finishedAt: Instant): String {
    val configuration = LocalConfiguration.current
    val millis = remember(startedAt, finishedAt) {
        Duration.between(startedAt, finishedAt).toMillis()
    }

    if (millis < MillisInSecond) {
        return stringResource(R.string.sync_status_duration_millis, millis)
    }

    val seconds = remember(millis, configuration) {
        NumberFormat.getNumberInstance(configuration.locales[0]).apply {
            minimumFractionDigits = SecondsFractionDigits
            maximumFractionDigits = SecondsFractionDigits
        }.format(millis.toDouble() / MillisInSecond)
    }

    return stringResource(R.string.sync_status_duration_seconds, seconds)
}

private const val TimeSkeleton = "Hm"
private const val DateTimeSkeleton = "d MMM Hm"

private val SyncStatusUiState.Content.isSyncing: Boolean
    get() = periodic.state == SyncState.Running || manual.state == SyncState.Running

private val SyncTrigger.labelRes: Int
    get() = when (this) {
        SyncTrigger.Periodic -> R.string.sync_status_trigger_periodic
        SyncTrigger.Manual -> R.string.sync_status_trigger_manual
    }

private val SyncState.labelRes: Int
    get() = when (this) {
        SyncState.Idle -> R.string.sync_state_idle
        SyncState.Scheduled -> R.string.sync_state_scheduled
        SyncState.Running -> R.string.sync_state_running
        SyncState.Succeeded -> R.string.sync_state_succeeded
        SyncState.Failed -> R.string.sync_state_failed
    }

private val SyncResult.labelRes: Int
    get() = when (this) {
        SyncResult.Success -> R.string.sync_result_success
        SyncResult.Retry -> R.string.sync_result_retry
        SyncResult.Failed -> R.string.sync_result_failed
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

private val SourceId.labelRes: Int
    get() = when (this) {
        SourceId.FRANKFURTER -> R.string.sync_status_source_frankfurter
        SourceId.COINGECKO -> R.string.sync_status_source_coingecko
        SourceId.CBR -> R.string.sync_status_source_cbr
    }

@Composable
private fun SyncState.badgeContainerColor(): Color = when (this) {
    SyncState.Running, SyncState.Succeeded -> KursorTheme.colors.rateUpContainer
    SyncState.Scheduled -> KursorTheme.colors.staleContainer
    SyncState.Failed -> KursorTheme.colors.rateDownContainer
    SyncState.Idle -> KursorTheme.colorScheme.surfaceVariant
}

@Composable
private fun SyncState.badgeContentColor(): Color = when (this) {
    SyncState.Running, SyncState.Succeeded -> KursorTheme.colors.rateUp
    SyncState.Scheduled -> KursorTheme.colors.stale
    SyncState.Failed -> KursorTheme.colors.rateDown
    SyncState.Idle -> KursorTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun SyncResult.badgeContainerColor(): Color = when (this) {
    SyncResult.Success -> KursorTheme.colors.rateUpContainer
    SyncResult.Retry -> KursorTheme.colors.staleContainer
    SyncResult.Failed -> KursorTheme.colors.rateDownContainer
}

@Composable
private fun SyncResult.badgeContentColor(): Color = when (this) {
    SyncResult.Success -> KursorTheme.colors.rateUp
    SyncResult.Retry -> KursorTheme.colors.stale
    SyncResult.Failed -> KursorTheme.colors.rateDown
}

private fun previewContent(): SyncStatusUiState.Content = SyncStatusUiState.Content(
    periodic = SyncQueue(
        state = SyncState.Scheduled,
        nextRunAt = Instant.now().plusSeconds(252 * 60),
        attemptCount = 0,
    ),
    manual = SyncQueue(
        state = SyncState.Scheduled,
        nextRunAt = Instant.now().plusSeconds(120),
        attemptCount = 2,
    ),
    runs = listOf(
        previewRun(id = 1, minutesAgo = 4, result = SyncResult.Success, updatedCount = 18),
        previewRun(
            id = 2,
            minutesAgo = 19,
            result = SyncResult.Retry,
            errorKind = SyncErrorKind.RateLimited,
            attempt = 2,
        ),
        previewRun(id = 3, minutesAgo = 21, result = SyncResult.Success, updatedCount = 5),
        previewRun(
            id = 4,
            minutesAgo = 96,
            result = SyncResult.Failed,
            errorKind = SyncErrorKind.NoNetwork,
            attempt = 3,
        ),
    ),
)

private fun previewRun(
    id: Long,
    minutesAgo: Long,
    result: SyncResult,
    updatedCount: Int? = null,
    errorKind: SyncErrorKind? = null,
    attempt: Int = 1,
): SyncRun {
    val finishedAt = Instant.parse("2026-10-14T09:38:00Z").minusSeconds(minutesAgo * 60)

    return SyncRun(
        sourceId = SourceId.FRANKFURTER,
        trigger = SyncTrigger.Periodic,
        startedAt = finishedAt.minusMillis(1_200),
        finishedAt = finishedAt,
        result = result,
        errorKind = errorKind,
        updatedCount = updatedCount,
        attempt = attempt,
        id = id,
    )
}

@PreviewLightDark
@Preview(locale = "ru", showBackground = true, backgroundColor = 0xFFF7F6F3)
@Composable
private fun SyncStatusContentPreview() {
    KursorTheme {
        SyncStatusContent(
            state = previewContent(),
            onBackClick = {},
            onAction = {},
        )
    }
}

@Preview(widthDp = 844, heightDp = 390, locale = "ru")
@Composable
private fun SyncStatusPanesPreview() {
    KursorTheme {
        SyncStatusContent(
            state = previewContent(),
            onBackClick = {},
            onAction = {},
        )
    }
}
