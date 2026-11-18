package dev.faizksen.kursor.watchlist.pairdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorRadius
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.designsystem.components.ScreenTopBar
import dev.faizksen.kursor.designsystem.components.TopAppBarButton
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.watchlist.R
import dev.faizksen.kursor.watchlist.addpair.cadenceText
import dev.faizksen.kursor.watchlist.displayedPair
import dev.faizksen.kursor.watchlist.displayedValue
import dev.faizksen.kursor.watchlist.rememberLongDateText
import dev.faizksen.kursor.watchlist.rememberRateValueText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PairDetailScreen(
    onBackClick: () -> Unit,
    onChangeProviderClick: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: PairDetailViewModel = koinViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    PairDetailEffects(effects = viewModel.effect, onDeleted = onDeleted)

    PairDetailContent(
        state = state,
        onBackClick = onBackClick,
        onChangeProviderClick = onChangeProviderClick,
        onAction = viewModel::obtainAction,
    )
}

@Composable
internal fun PairDetailContent(
    state: PairDetailUiState,
    onBackClick: () -> Unit,
    onChangeProviderClick: () -> Unit,
    onAction: (PairDetailUiAction) -> Unit,
) {
    Scaffold(
        containerColor = KursorTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(
                onNavButtonClick = onBackClick,
                title = state.titleText(),
                actions = {
                    if (state is PairDetailUiState.Watching) {
                        PairMenu(
                            onChangeProviderClick = onChangeProviderClick,
                            onFlipClick = { onAction(PairDetailUiAction.FlipClicked) },
                            onDeleteClick = { onAction(PairDetailUiAction.DeleteClicked) },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state) {
                PairDetailUiState.Loading -> Centred { CircularProgressIndicator() }

                PairDetailUiState.Gone -> Centred {
                    Message(text = stringResource(R.string.pair_detail_gone))
                }

                is PairDetailUiState.Watching -> Watching(
                    state = state,
                    onChangeProviderClick = onChangeProviderClick,
                )
            }
        }
    }
}

@Composable
private fun Watching(state: PairDetailUiState.Watching, onChangeProviderClick: () -> Unit) {
    val rate = state.watched.rate
    val value = state.watched.displayedValue()
    val issuer = state.issuerName()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = KursorDimens.screenHorizontalPadding,
                end = KursorDimens.screenHorizontalPadding,
                top = KursorDimens.sectionSpacing,
            ),
        verticalArrangement = Arrangement.spacedBy(KursorDimens.sectionSpacing),
    ) {
        if (value == null) {
            Text(
                text = stringResource(R.string.pair_detail_no_rate),
                style = KursorTheme.typography.bodyLarge,
                color = KursorTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = rememberRateValueText(value),
                style = KursorTheme.typography.displaySmall,
                color = KursorTheme.colorScheme.onBackground,
            )
        }

        if (state.watched.quiet && issuer != null && rate != null) {
            QuietNote(
                issuer = issuer,
                observedOn = rate.date,
                onClick = onChangeProviderClick,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(KursorDimens.sectionHeaderPaddingBottom),
        ) {
            Meta(text = sourceText(issuer = issuer, rate = rate))

            state.provider?.let { provider ->
                Meta(
                    text = stringResource(
                        R.string.pair_detail_cadence,
                        cadenceText(provider.cadence),
                    ),
                )
            }

            Meta(
                text = stringResource(
                    R.string.pair_detail_added,
                    rememberLongDateText(state.watched.watchedPair.addedOn()),
                ),
            )
        }
    }
}

@Composable
private fun QuietNote(issuer: String, observedOn: LocalDate, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = KursorTheme.colors.staleContainer,
        shape = RoundedCornerShape(KursorRadius.cardCompact),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(
                R.string.pair_detail_quiet,
                issuer,
                rememberLongDateText(observedOn),
            ),
            style = KursorTheme.typography.bodyMedium,
            color = KursorTheme.colors.stale,
            modifier = Modifier.padding(
                horizontal = KursorDimens.cardCompactPaddingHorizontal,
                vertical = KursorDimens.cardCompactPaddingVertical,
            ),
        )
    }
}

@Composable
private fun PairMenu(
    onChangeProviderClick: () -> Unit,
    onFlipClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBarButton(
        icon = Icons.Default.MoreVert,
        contentDescription = stringResource(R.string.pair_detail_menu),
        onClick = { expanded = true },
    )

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.pair_detail_change_provider)) },
            onClick = {
                expanded = false
                onChangeProviderClick()
            },
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.pair_detail_flip)) },
            onClick = {
                expanded = false
                onFlipClick()
            },
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.pair_detail_delete),
                    color = KursorTheme.colorScheme.error,
                )
            },
            onClick = {
                expanded = false
                onDeleteClick()
            },
        )
    }
}

@Composable
private fun sourceText(issuer: String?, rate: RatePoint?): String = when {
    issuer == null -> stringResource(R.string.pair_detail_source_unknown)
    rate == null -> issuer
    else -> stringResource(
        R.string.pair_detail_source,
        issuer,
        rememberLongDateText(rate.date),
    )
}

@Composable
private fun Meta(text: String) {
    Text(
        text = text,
        style = KursorTheme.typography.labelMedium,
        color = KursorTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Message(text: String) {
    Text(
        text = text,
        style = KursorTheme.typography.bodyMedium,
        color = KursorTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = KursorDimens.screenHorizontalPadding),
    )
}

@Composable
private fun Centred(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun PairDetailUiState.titleText(): String = if (this is PairDetailUiState.Watching) {
    watched.watchedPair.displayedPair().let { shown ->
        stringResource(R.string.watchlist_pair, shown.base.code, shown.quote.code)
    }
} else {
    ""
}

private fun PairDetailUiState.Watching.issuerName(): String? =
    provider?.name ?: watched.watchedPair.provider?.key

private fun WatchedPair.addedOn(): LocalDate =
    addedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date

@PreviewLightDark
@Composable
private fun PairDetailPreview() {
    KursorTheme {
        PairDetailContent(
            state = PairDetailUiState.Watching(
                watched = previewPair(),
                provider = previewProvider(),
            ),
            onBackClick = {},
            onChangeProviderClick = {},
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PairDetailQuietPreview() {
    KursorTheme {
        PairDetailContent(
            state = PairDetailUiState.Watching(
                watched = previewPair().copy(quiet = true),
                provider = previewProvider(),
            ),
            onBackClick = {},
            onChangeProviderClick = {},
            onAction = {},
        )
    }
}

private fun previewProvider() = Provider(
    key = ProviderKey("NBP"),
    name = "Narodowy Bank Polski",
    rateType = "official",
    pivotCurrency = CurrencyCode("PLN"),
    cadence = PublishCadence.DAILY,
    latestDate = LocalDate.parse("2026-11-17"),
    dataUrl = null,
)

private fun previewPair() = WatchedPairWithRate(
    watchedPair = WatchedPair(
        id = 1,
        pair = CurrencyPair(CurrencyCode("EUR"), CurrencyCode("PLN")),
        provider = ProviderKey("NBP"),
        position = 0,
        addedAt = Instant.parse("2026-11-01T09:00:00Z"),
    ),
    rate = RatePoint(
        date = LocalDate.parse("2026-11-17"),
        value = Rate.parse("4.4032")!!,
        sourceId = SourceId.FRANKFURTER,
        provider = ProviderKey("NBP"),
    ),
)
