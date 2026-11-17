package dev.faizksen.kursor.watchlist.addpair

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorRadius
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.watchlist.R

private val RadioSize = 20.dp
private val RadioDotSize = 10.dp
private val MarkerBorderWidth = 2.dp
private val CardBorderWidth = 1.dp
private val ProviderRowPaddingVertical = 12.dp
private val ProviderCardPaddingVertical = 4.dp
private val RowTitleSpacing = 2.dp

@Composable
internal fun ProviderList(
    providers: List<Provider>,
    selected: ProviderKey?,
    everyProviderCount: Int,
    showingEveryProvider: Boolean,
    onPick: (ProviderKey) -> Unit,
    onShowEvery: () -> Unit,
    header: @Composable () -> Unit = {},
) {
    if (providers.isEmpty()) {
        header()
        Note(text = stringResource(R.string.add_pair_no_providers))
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
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
                    providers.forEachIndexed { index, provider ->
                        ProviderRow(
                            provider = provider,
                            recommended = index == 0,
                            selected = provider.key == selected,
                            onClick = { onPick(provider.key) },
                        )
                        if (index != providers.lastIndex) {
                            HorizontalDivider(color = KursorTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }

        if (!showingEveryProvider) {
            item {
                Note(
                    text = stringResource(R.string.add_pair_show_all, everyProviderCount),
                    color = KursorTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onShowEvery),
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
                text = cadenceText(provider.cadence),
                style = KursorTheme.typography.labelMedium,
                color = KursorTheme.colorScheme.onSurfaceVariant,
            )
        }
        Radio(selected = selected)
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
