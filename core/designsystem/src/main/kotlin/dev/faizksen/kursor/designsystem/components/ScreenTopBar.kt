package dev.faizksen.kursor.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(
    onNavButtonClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    navButtonIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navButtonContentDescription: String = stringResource(R.string.ds_navigate_back),
    lifted: Boolean = false,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    val elevation by animateDpAsState(
        targetValue = if (lifted) 2.dp else 0.dp,
        label = "topBarElevation",
    )
    Surface(shadowElevation = elevation, modifier = modifier) {
        TopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = onNavButtonClick,
                    modifier = Modifier.semantics {
                        contentDescription = navButtonContentDescription
                    }
                ) {
                    Icon(
                        imageVector = navButtonIcon,
                        contentDescription = null
                    )
                }
            },
            title = {
                Text(
                    style = KursorTheme.typography.titleLarge,
                    text = title
                )
            },
            actions = actions
        )
    }
}