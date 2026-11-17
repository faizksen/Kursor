package dev.faizksen.kursor.watchlist.addpair

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorRadius
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.designsystem.components.KursorButton
import dev.faizksen.kursor.designsystem.components.ScreenTopBar
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.watchlist.R
import org.koin.compose.viewmodel.koinViewModel

private val StepBarHeight = 3.dp
private val StepBarRadius = 2.dp
private val StepSpacing = 8.dp
private val StepLabelSpacing = 4.dp
private val RowTitleSpacing = 2.dp
private val MarkerSize = 22.dp
private val RadioSize = 20.dp
private val RadioDotSize = 10.dp
private val MarkerBorderWidth = 2.dp
private val SelectedBorderWidth = 1.5.dp
private val CardBorderWidth = 1.dp
private val ProviderRowPaddingVertical = 12.dp
private val ProviderCardPaddingVertical = 4.dp
private val NotePaddingTop = 6.dp
private val NoteInset = 4.dp
private val SectionLetterSpacing = 1.sp

@Composable
fun AddPairScreen(
    onDismiss: () -> Unit,
    onAdded: () -> Unit,
    viewModel: AddPairViewModel = koinViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    AddPairEffects(effects = viewModel.effect, onDismiss = onDismiss, onAdded = onAdded)

    AddPairContent(state = state, onAction = viewModel::obtainAction)
}

@Composable
internal fun AddPairContent(
    state: AddPairUiState,
    onAction: (AddPairUiAction) -> Unit,
) {
    Scaffold(
        containerColor = KursorTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(
                onNavButtonClick = { onAction(AddPairUiAction.BackClicked) },
                title = stringResource(R.string.add_pair_title),
                navButtonIcon = Icons.Default.Close,
                navButtonContentDescription = stringResource(R.string.add_pair_close),
            )
        },
        bottomBar = {
            if (state is AddPairUiState.Picking) {
                AddPairBottomBar(state = state, onAction = onAction)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state) {
                AddPairUiState.Loading -> Loading()
                AddPairUiState.CatalogueTooOld -> CatalogueTooOld()
                is AddPairUiState.Picking -> Picking(state = state, onAction = onAction)
            }
        }
    }
}

@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CatalogueTooOld() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KursorDimens.screenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = KursorTheme.colors.staleContainer,
            shape = RoundedCornerShape(KursorRadius.cardCompact),
        ) {
            Text(
                text = stringResource(R.string.add_pair_too_old),
                style = KursorTheme.typography.bodyMedium,
                color = KursorTheme.colors.stale,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = KursorDimens.cardCompactPaddingHorizontal,
                    vertical = KursorDimens.cardCompactPaddingVertical,
                ),
            )
        }
    }
}

@Composable
private fun Picking(
    state: AddPairUiState.Picking,
    onAction: (AddPairUiAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        StepIndicator(step = state.step)

        when (state.step) {
            AddPairStep.BASE, AddPairStep.QUOTE -> CurrencyPicker(state = state, onAction = onAction)
            AddPairStep.PROVIDER -> ProviderPicker(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun StepIndicator(step: AddPairStep) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(StepSpacing),
        modifier = Modifier.padding(
            start = KursorDimens.screenHorizontalPadding,
            end = KursorDimens.screenHorizontalPadding,
            bottom = KursorDimens.sectionSpacing,
        ),
    ) {
        AddPairStep.entries.forEach { entry ->
            Step(
                label = stringResource(entry.labelRes),
                reached = entry.ordinal <= step.ordinal,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Step(
    label: String,
    reached: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (reached) {
        KursorTheme.colorScheme.primary
    } else {
        KursorTheme.colorScheme.outlineVariant
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(StepLabelSpacing),
        modifier = modifier,
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(StepBarRadius),
            modifier = Modifier
                .fillMaxWidth()
                .height(StepBarHeight),
        ) {}
        Text(
            text = label,
            style = KursorTheme.typography.labelSmall,
            color = if (reached) color else KursorTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CurrencyPicker(
    state: AddPairUiState.Picking,
    onAction: (AddPairUiAction) -> Unit,
) {
    val nameOf = rememberCurrencyNamer()
    val matches = remember(state.currencies, state.query, state.base, state.step, nameOf) {
        state.currencies.matching(
            query = state.query,
            excluding = state.base.takeIf { state.step == AddPairStep.QUOTE },
            nameOf = nameOf,
        )
    }

    OutlinedTextField(
        value = state.query,
        onValueChange = { query -> onAction(AddPairUiAction.QueryChanged(query)) },
        singleLine = true,
        shape = RoundedCornerShape(KursorRadius.card),
        placeholder = { Text(text = stringResource(R.string.add_pair_search_hint)) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KursorDimens.screenHorizontalPadding),
    )

    SectionHeader(
        text = if (state.query.isBlank()) {
            stringResource(R.string.add_pair_currencies_header)
        } else {
            stringResource(R.string.add_pair_matches, matches.size)
        },
    )

    if (matches.isEmpty()) {
        Note(text = stringResource(R.string.add_pair_nothing_found, state.query.trim()))
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = KursorDimens.screenHorizontalPadding,
            end = KursorDimens.screenHorizontalPadding,
            bottom = KursorDimens.sectionSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
    ) {
        items(items = matches, key = { currency -> currency.code.code }) { currency ->
            RowCard(
                selected = false,
                onClick = { onAction(AddPairUiAction.CurrencyPicked(currency.code)) },
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currency.code.code,
                        style = KursorTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = nameOf(currency),
                        style = KursorTheme.typography.labelMedium,
                        color = KursorTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = RowTitleSpacing),
                    )
                }
                Marker(selected = false)
            }
        }

        item {
            Note(text = stringResource(R.string.add_pair_search_note))
        }
    }
}

@Composable
private fun ProviderPicker(
    state: AddPairUiState.Picking,
    onAction: (AddPairUiAction) -> Unit,
) {
    PairSummary(base = state.base, quote = state.quote)

    SectionHeader(text = stringResource(R.string.add_pair_providers_header))

    if (state.providers.isEmpty()) {
        Note(text = stringResource(R.string.add_pair_no_providers))
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = KursorDimens.screenHorizontalPadding,
            end = KursorDimens.screenHorizontalPadding,
            bottom = KursorDimens.sectionSpacing,
        ),
    ) {
        item {
            Surface(
                color = KursorTheme.colorScheme.surface,
                shape = RoundedCornerShape(KursorRadius.card),
                border = BorderStroke(CardBorderWidth, KursorTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = KursorDimens.cardPaddingHorizontal,
                        vertical = ProviderCardPaddingVertical,
                    ),
                ) {
                    state.providers.forEachIndexed { index, provider ->
                        ProviderRow(
                            provider = provider,
                            recommended = index == 0,
                            selected = provider.key == state.provider,
                            onClick = { onAction(AddPairUiAction.ProviderPicked(provider.key)) },
                        )
                        if (index != state.providers.lastIndex) {
                            HorizontalDivider(color = KursorTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }

        if (!state.showingEveryProvider) {
            item {
                Note(
                    text = stringResource(R.string.add_pair_show_all, state.everyProviderCount),
                    modifier = Modifier.clickable {
                        onAction(AddPairUiAction.ShowEveryProviderClicked)
                    },
                )
            }
        }
    }
}

@Composable
private fun ProviderRow(
    provider: Provider,
    recommended: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = ProviderRowPaddingVertical),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KursorDimens.listItemSpacing),
            ) {
                Text(
                    text = provider.key.key,
                    style = KursorTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                if (recommended) {
                    Text(
                        text = stringResource(R.string.add_pair_recommended),
                        style = KursorTheme.typography.labelSmall,
                        color = KursorTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = provider.name,
                style = KursorTheme.typography.labelMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = RowTitleSpacing),
            )
            Text(
                text = providerMetaText(provider),
                style = KursorTheme.typography.labelMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
            )
        }
        Radio(selected = selected)
    }
}

@Composable
private fun PairSummary(base: CurrencyCode?, quote: CurrencyCode?) {
    if (base == null || quote == null) return

    Surface(
        color = KursorTheme.colorScheme.surface,
        shape = RoundedCornerShape(KursorRadius.card),
        border = BorderStroke(CardBorderWidth, KursorTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = KursorDimens.screenHorizontalPadding,
                end = KursorDimens.screenHorizontalPadding,
            ),
    ) {
        Text(
            text = stringResource(R.string.add_pair_tracking, base.code, quote.code),
            style = KursorTheme.typography.titleMedium,
            modifier = Modifier.padding(
                horizontal = KursorDimens.cardPaddingHorizontal,
                vertical = KursorDimens.cardPaddingVertical,
            ),
        )
    }
}

@Composable
private fun RowCard(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        color = KursorTheme.colorScheme.surface,
        shape = RoundedCornerShape(KursorRadius.card),
        border = if (selected) {
            BorderStroke(SelectedBorderWidth, KursorTheme.colorScheme.primary)
        } else {
            BorderStroke(CardBorderWidth, KursorTheme.colorScheme.outlineVariant)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = KursorDimens.cardPaddingHorizontal,
                vertical = KursorDimens.cardPaddingVertical,
            ),
            content = content,
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = KursorTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = SectionLetterSpacing,
        color = KursorTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            start = KursorDimens.screenHorizontalPadding + NoteInset,
            end = KursorDimens.screenHorizontalPadding,
            top = KursorDimens.sectionHeaderPaddingTop,
            bottom = KursorDimens.sectionHeaderPaddingBottom,
        ),
    )
}

@Composable
private fun Note(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = KursorTheme.typography.labelLarge,
        color = KursorTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(
            start = KursorDimens.screenHorizontalPadding + NoteInset,
            end = KursorDimens.screenHorizontalPadding,
            top = NotePaddingTop,
        ),
    )
}

@Composable
private fun Marker(selected: Boolean) {
    if (selected) {
        Surface(
            color = KursorTheme.colorScheme.primary,
            shape = RoundedCornerShape(MarkerSize / 2),
            modifier = Modifier.size(MarkerSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = KursorTheme.colorScheme.onPrimary,
                )
            }
        }
    } else {
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(MarkerSize / 2),
            border = BorderStroke(MarkerBorderWidth, KursorTheme.colorScheme.outlineVariant),
            modifier = Modifier.size(MarkerSize),
        ) {}
    }
}

@Composable
private fun Radio(selected: Boolean) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(RadioSize / 2),
        border = BorderStroke(
            MarkerBorderWidth,
            if (selected) {
                KursorTheme.colorScheme.primary
            } else {
                KursorTheme.colorScheme.outlineVariant
            },
        ),
        modifier = Modifier.size(RadioSize),
    ) {
        if (selected) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    color = KursorTheme.colorScheme.primary,
                    shape = RoundedCornerShape(RadioDotSize / 2),
                    modifier = Modifier.size(RadioDotSize),
                ) {}
            }
        }
    }
}

@Composable
private fun AddPairBottomBar(
    state: AddPairUiState.Picking,
    onAction: (AddPairUiAction) -> Unit,
) {
    if (state.step != AddPairStep.PROVIDER) return

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
                text = stringResource(
                    R.string.add_pair_confirm,
                    state.base?.code.orEmpty(),
                    state.quote?.code.orEmpty(),
                ),
                onClick = { onAction(AddPairUiAction.ConfirmClicked) },
                enabled = state.provider != null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val AddPairStep.labelRes: Int
    get() = when (this) {
        AddPairStep.BASE -> R.string.add_pair_step_base
        AddPairStep.QUOTE -> R.string.add_pair_step_quote
        AddPairStep.PROVIDER -> R.string.add_pair_step_provider
    }
