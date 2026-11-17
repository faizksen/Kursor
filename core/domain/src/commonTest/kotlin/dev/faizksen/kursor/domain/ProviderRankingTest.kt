package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ProviderRankingTest {

    @Test
    fun theIssuersOfEitherCurrencyComeBeforeEveryoneElse() {
        val ranked = listOf(CNB, NBP, ECB).rankFor(EUR_PLN).keys()

        assertEquals(listOf("NBP", "ECB", "CNB"), ranked)
    }

    @Test
    fun aProviderDeclaringNoRateTypeSinksBelowOneThatDoes() {
        val ranked = listOf(HNB, ECB).rankFor(EUR_PLN).keys()

        assertEquals(listOf("ECB", "HNB"), ranked)
    }

    @Test
    fun aWeeklyProviderSinksBelowTheDailyOnes() {
        val ranked = listOf(FRED, BOJ).rankFor(CurrencyPair(EUR, USD)).keys()

        assertEquals(listOf("BOJ", "FRED"), ranked)
    }

    @Test
    fun freshnessOutranksThePreferredRateType() {
        val ranked = listOf(ECB, STALE_OFFICIAL).rankFor(EUR_PLN).keys()

        assertEquals(listOf("ECB", "STALE"), ranked)
    }

    @Test
    fun aTieGoesToTheIssuerOfTheCurrencyThePriceIsExpressedIn() {
        assertEquals(listOf("NBP", "ECB"), listOf(ECB, NBP).rankFor(EUR_PLN).keys())
        assertEquals(listOf("ECB", "NBP"), listOf(NBP, ECB).rankFor(PLN_EUR).keys())
    }

    @Test
    fun providersThatTieOnEverythingKeepAStableOrder() {
        val first = HNB.copy(key = ProviderKey("AAA"))
        val second = HNB.copy(key = ProviderKey("ZZZ"))

        assertEquals(listOf("AAA", "ZZZ"), listOf(second, first).rankFor(EUR_PLN).keys())
    }

    @Test
    fun theZlotyIssuerLeadsItsOwnPairAheadOfTheEuroReferenceAndItsReprints() {
        val ranked = listOf(BDI, ECB, HNB, NBP, CBR).rankFor(EUR_PLN).keys()

        assertEquals(listOf("NBP", "ECB", "HNB", "BDI", "CBR"), ranked)
    }

    @Test
    fun theEuroReferenceLeadsAgainstTheDollarIssuersItIsFresherThan() {
        val ranked = listOf(FRED, BOJ, PMA, IMF, ECB, HNB).rankFor(CurrencyPair(EUR, USD)).keys()

        assertEquals(listOf("ECB", "IMF", "PMA", "BOJ", "FRED", "HNB"), ranked)
    }

    @Test
    fun theRoubleIssuerLeadsRoublePairs() {
        val ranked = listOf(ECB, HNB, CBR).rankFor(CurrencyPair(EUR, CurrencyCode("RUB"))).keys()

        assertEquals(listOf("CBR", "ECB", "HNB"), ranked)
    }

    @Test
    fun theShortlistIsJustTheTopWhenBothIssuersAreAlreadyInIt() {
        val ranked = listOf(NBP, ECB, HNB, BDI, CBR).rankFor(EUR_PLN)

        assertEquals(
            listOf("NBP", "ECB", "HNB", "BDI", "CBR"),
            ranked.topWithIssuers(EUR_PLN, PROVIDER_SHORTLIST).keys(),
        )
    }

    @Test
    fun anIssuerPushedOutOfTheTopFiveIsAddedBackToTheShortlist() {
        val dollarIssuers = listOf(IMF, PMA, BOJ, FRED, IMF.copy(key = ProviderKey("BOTA")))
        val ranked = (dollarIssuers + ECB + STALE_ZLOTY).rankFor(CurrencyPair(USD, PLN))

        val shortlist = ranked.topWithIssuers(CurrencyPair(USD, PLN), PROVIDER_SHORTLIST).keys()

        assertEquals(PROVIDER_SHORTLIST + 1, shortlist.size)
        assertEquals("STALE_ZLOTY", shortlist.last())
    }

    @Test
    fun theShortlistGrowsByOneAtMostBecauseTheBetterIssuerIsAlwaysInTheTop() {
        val pair = CurrencyPair(base = PLN, quote = CurrencyCode("CHF"))
        val zlotyIssuers = (1..5).map { index ->
            NBP.copy(key = ProviderKey("NBP$index"))
        }
        val staleFrancIssuer = provider("ESTV", "CHF", null, latestDate = "2026-01-01")

        val shortlist = (zlotyIssuers + staleFrancIssuer)
            .rankFor(pair)
            .topWithIssuers(pair, PROVIDER_SHORTLIST)
            .keys()

        assertEquals(PROVIDER_SHORTLIST + 1, shortlist.size)
        assertEquals("ESTV", shortlist.last())
    }

    private fun List<Provider>.keys(): List<String> = map { provider -> provider.key.key }

    private companion object {
        val EUR = CurrencyCode("EUR")
        val USD = CurrencyCode("USD")
        val PLN = CurrencyCode("PLN")

        val EUR_PLN = CurrencyPair(base = EUR, quote = PLN)
        val PLN_EUR = CurrencyPair(base = PLN, quote = EUR)

        fun provider(
            key: String,
            pivot: String,
            rateType: String?,
            cadence: PublishCadence = PublishCadence.DAILY,
            latestDate: String? = "2026-11-17",
        ) = Provider(
            key = ProviderKey(key),
            name = key,
            rateType = rateType,
            pivotCurrency = CurrencyCode(pivot),
            cadence = cadence,
            latestDate = latestDate?.let { date -> LocalDate.parse(date) },
            dataUrl = null,
        )

        val ECB = provider("ECB", "EUR", "reference rate")
        val NBP = provider("NBP", "PLN", "average rate")
        val CBR = provider("CBR", "RUB", "official rate")
        val HNB = provider("HNB", "EUR", null)
        val BDI = provider("BDI", "EUR", null, latestDate = "2026-11-16")
        val IMF = provider("IMF", "USD", "representative rate", latestDate = "2026-11-16")
        val PMA = provider("PMA", "USD", "middle rate", latestDate = "2026-11-16")
        val BOJ = provider("BOJ", "USD", "Tokyo market interbank", latestDate = "2026-11-13")
        val FRED = provider("FRED", "USD", "H.10", PublishCadence.WEEKLY, "2026-11-13")
        val CNB = provider("CNB", "CZK", "exchange rate fixing")

        val STALE_OFFICIAL = provider("STALE", "PLN", "official rate", latestDate = "2026-11-10")
        val STALE_ZLOTY = provider("STALE_ZLOTY", "PLN", null, latestDate = "2026-01-01")
    }
}
