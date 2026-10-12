package dev.faizksen.kursor.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.faizksen.kursor.designsystem.KursorDimens
import dev.faizksen.kursor.designsystem.KursorRadius
import dev.faizksen.kursor.designsystem.KursorTextStyles
import dev.faizksen.kursor.designsystem.KursorTheme

private val ButtonContentPaddingHorizontal = 22.dp
private val SmallButtonContentPaddingHorizontal = 14.dp
private val SmallButtonContentPaddingVertical = 8.dp

@Composable
fun KursorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = KursorDimens.bottomButtonHeight,
    containerColor: Color = KursorTheme.colorScheme.primary,
    contentColor: Color = KursorTheme.colorScheme.onPrimary,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = minHeight),
        enabled = enabled,
        shape = RoundedCornerShape(KursorRadius.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(horizontal = ButtonContentPaddingHorizontal),
    ) {
        Text(text = text, style = KursorTextStyles.button)
    }
}

@Composable
fun KursorSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = KursorTheme.colorScheme.primary,
    contentColor: Color = KursorTheme.colorScheme.onPrimary,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(KursorRadius.smallButton),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(
            horizontal = SmallButtonContentPaddingHorizontal,
            vertical = SmallButtonContentPaddingVertical,
        ),
    ) {
        Text(text = text, style = KursorTextStyles.buttonSmall)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6F3)
@Composable
private fun KursorButtonPreview() {
    KursorTheme {
        KursorButton(text = "Добавить пару", onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6F3)
@Composable
private fun KursorSmallButtonPreview() {
    KursorTheme {
        KursorSmallButton(
            text = "Повторить",
            onClick = {},
            containerColor = KursorTheme.colorScheme.error,
            contentColor = KursorTheme.colorScheme.onError,
        )
    }
}
