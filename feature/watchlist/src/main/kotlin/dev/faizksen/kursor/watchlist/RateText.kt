package dev.faizksen.kursor.watchlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import dev.faizksen.kursor.model.Rate
import java.math.RoundingMode
import java.text.NumberFormat

private const val MinimumFractionDigits = 2
private const val MaximumFractionDigits = 4

@Composable
internal fun rememberRateValueText(value: Rate): String {
    val configuration = LocalConfiguration.current

    return remember(value, configuration) {
        NumberFormat.getNumberInstance(configuration.locales[0]).apply {
            minimumFractionDigits = MinimumFractionDigits
            maximumFractionDigits = MaximumFractionDigits
            roundingMode = RoundingMode.HALF_UP
        }.format(value.toPlainString().toBigDecimal())
    }
}
