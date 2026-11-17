package dev.faizksen.kursor.watchlist.addpair

import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyType
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFilterTest {

    @Test
    fun anEmptyQueryKeepsEverything() {
        assertEquals(
            listOf("PLN", "USD", "CHF"),
            CURRENCIES.matching(query = "", excluding = null, nameOf = NAMES).codes(),
        )
    }

    @Test
    fun aQueryMatchesTheCode() {
        assertEquals(
            listOf("PLN"),
            CURRENCIES.matching(query = "pl", excluding = null, nameOf = NAMES).codes(),
        )
    }

    @Test
    fun aQueryMatchesTheLocalizedName() {
        assertEquals(
            listOf("PLN"),
            CURRENCIES.matching(query = "злот", excluding = null, nameOf = NAMES).codes(),
        )
    }

    @Test
    fun aQueryIsTrimmedAndCaseIsIgnored() {
        assertEquals(
            listOf("CHF"),
            CURRENCIES.matching(query = "  ФРАНК ", excluding = null, nameOf = NAMES).codes(),
        )
    }

    @Test
    fun theCurrencyAlreadyPickedIsNotOfferedAgain() {
        assertEquals(
            listOf("USD", "CHF"),
            CURRENCIES
                .matching(query = "", excluding = CurrencyCode("PLN"), nameOf = NAMES)
                .codes(),
        )
    }

    @Test
    fun theExclusionAppliesWhileSearchingToo() {
        assertEquals(
            emptyList<String>(),
            CURRENCIES
                .matching(query = "злот", excluding = CurrencyCode("PLN"), nameOf = NAMES)
                .codes(),
        )
    }

    @Test
    fun aQueryNothingMatchesReturnsNothing() {
        assertEquals(
            emptyList<String>(),
            CURRENCIES.matching(query = "крона", excluding = null, nameOf = NAMES).codes(),
        )
    }

    private fun List<Currency>.codes(): List<String> = map { currency -> currency.code.code }

    private companion object {
        fun fiat(code: String) = Currency(
            code = CurrencyCode(code),
            name = code,
            symbol = code,
            type = CurrencyType.FIAT,
        )

        val CURRENCIES = listOf(fiat("PLN"), fiat("USD"), fiat("CHF"))

        val NAMES = CurrencyNamer { currency ->
            when (currency.code.code) {
                "PLN" -> "Польский злотый"
                "USD" -> "Доллар США"
                else -> "Швейцарский франк"
            }
        }
    }
}
