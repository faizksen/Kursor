package dev.faizksen.kursor.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootTopBar(
    title: String = "",
    lifted: Boolean = false,
    collapsed: Boolean = false,
    compactContent: @Composable () -> Unit = {},
    actions:  @Composable (RowScope.() -> Unit) = {}
) {
    val elevation by animateDpAsState(
        targetValue = if (lifted) 2.dp else 0.dp,
        label = "topBarElevation",
    )
    Surface(shadowElevation = elevation) {
        TopAppBar(
            windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        style = KursorTheme.typography.headlineMedium,
                        text = title
                    )
                    AnimatedVisibility(visible = collapsed) {
                        compactContent()
                    }
                }
            },
            actions = actions
        )
    }
}

@Composable
fun TopAppBarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Surface(
            shape = CircleShape,
            color = KursorTheme.colorScheme.surfaceVariant,
            shadowElevation = 0.dp,
            modifier = Modifier.size(KursorDimens.iconButtonSize),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = KursorTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Preview
@Composable
private fun TopAppBarButtonPreview() {
    KursorTheme {
        TopAppBarButton(
            icon = Icons.Default.Refresh,
            onClick = { },
            contentDescription = "Refresh",
            enabled = true
        )
    }
}

@Preview
@Composable
private fun RootTopAppBarPreview() {
    KursorTheme {
        RootTopBar(
            title = "Rates",
            lifted = true,
            collapsed = true,
            compactContent = { Text(
                "Refreshed at 9:12",
                style = KursorTheme.typography.labelMedium,
                modifier = Modifier.padding(start = KursorDimens.screenHorizontalPadding)) },
            actions = {
                TopAppBarButton(
                    icon = Icons.Default.Refresh,
                    onClick = { },
                    contentDescription = "Refresh",
                    enabled = true
                )
                TopAppBarButton(
                    icon = Icons.Default.MoreVert,
                    onClick = { },
                    contentDescription = "More"
                )
            }
        )
    }
}