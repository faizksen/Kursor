package dev.faizksen.kursor.watchlist

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorRadius
import dev.faizksen.kursor.designsystem.KursorTextStyles
import dev.faizksen.kursor.designsystem.KursorTheme

internal const val CrampedFromFontScale = 1.3f

@Composable
internal fun AddPairFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.watchlist_add_pair)
    val shape = RoundedCornerShape(KursorRadius.fab)
    val named = modifier.semantics { contentDescription = description }

    if (LocalDensity.current.fontScale >= CrampedFromFontScale) {
        FloatingActionButton(
            onClick = onClick,
            modifier = named,
            shape = shape,
            containerColor = KursorTheme.colorScheme.primary,
            contentColor = KursorTheme.colorScheme.onPrimary,
        ) {
            AddIcon()
        }
    } else {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = named,
            shape = shape,
            containerColor = KursorTheme.colorScheme.primary,
            contentColor = KursorTheme.colorScheme.onPrimary,
        ) {
            AddIcon()
            Spacer(modifier = Modifier.width(KursorDimens.fabContentSpacing))
            Text(
                text = stringResource(R.string.watchlist_add_pair_label),
                style = KursorTextStyles.button,
            )
        }
    }
}

@Composable
private fun AddIcon() {
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = null,
        modifier = Modifier.size(KursorDimens.iconSize),
    )
}
