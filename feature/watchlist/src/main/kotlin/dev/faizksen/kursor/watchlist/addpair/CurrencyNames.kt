package dev.faizksen.kursor.watchlist.addpair

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale as ComposeLocale
import dev.faizksen.kursor.model.Currency
import java.util.Locale
import java.util.Currency as JavaCurrency

internal fun interface CurrencyNamer {
    operator fun invoke(currency: Currency): String
}

@Composable
internal fun rememberCurrencyNamer(): CurrencyNamer {
    val languageTag = ComposeLocale.current.toLanguageTag()

    return remember(languageTag) {
        val locale = Locale.forLanguageTag(languageTag)
        val resolved = mutableMapOf<String, String>()

        CurrencyNamer { currency ->
            resolved.getOrPut(currency.code.code) {
                localizedCurrencyName(currency.code.code, locale) ?: currency.name
            }
        }
    }
}

private fun localizedCurrencyName(code: String, locale: Locale): String? = try {
    JavaCurrency.getInstance(code).getDisplayName(locale).takeIf { name -> name != code }
} catch (e: IllegalArgumentException) {
    null
}
