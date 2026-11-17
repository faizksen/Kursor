package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.CatalogueRepository
import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ProviderCandidatesUseCaseTest {

    private val catalogue = object : CatalogueRepository {
        var served: List<Provider> = emptyList()
        var askedFor: CurrencyPair? = null

        override suspend fun fetchedAt(): Instant? = null
        override suspend fun refresh() = Unit
        override suspend fun fiatCurrencies(): List<Currency> = emptyList()

        override suspend fun providers(keys: List<ProviderKey>): List<Provider> = emptyList()

        override suspend fun providersFor(pair: CurrencyPair): List<Provider> {
            askedFor = pair
            return served
        }
    }

    private val candidates = ProviderCandidatesUseCaseImpl(catalogue)

    @Test
    fun theShortlistIsRankedAndCappedWhileEveryCandidateStaysAvailable() = runTest {
        catalogue.served = listOf(NBP, ECB) + (1..8).map { index ->
            ECB.copy(key = ProviderKey("OTHER$index"), pivotCurrency = CurrencyCode("CZK"))
        }

        val result = candidates(EUR_PLN)

        assertEquals(EUR_PLN, catalogue.askedFor)
        assertEquals(listOf("NBP", "ECB"), result.shortlist.take(2).map { it.key.key })
        assertEquals(PROVIDER_SHORTLIST, result.shortlist.size)
        assertEquals(10, result.all.size)
        assertEquals(result.all.take(PROVIDER_SHORTLIST), result.shortlist)
    }

    @Test
    fun aPairNothingServesHasNoCandidates() = runTest {
        catalogue.served = emptyList()

        val result = candidates(EUR_PLN)

        assertEquals(emptyList(), result.shortlist)
        assertEquals(emptyList(), result.all)
    }

    private companion object {
        val EUR_PLN = CurrencyPair(base = CurrencyCode("EUR"), quote = CurrencyCode("PLN"))

        val ECB = Provider(
            key = ProviderKey("ECB"),
            name = "European Central Bank",
            rateType = "reference rate",
            pivotCurrency = CurrencyCode("EUR"),
            cadence = PublishCadence.DAILY,
            latestDate = LocalDate.parse("2026-11-17"),
            dataUrl = null,
        )

        val NBP = ECB.copy(
            key = ProviderKey("NBP"),
            name = "Narodowy Bank Polski",
            rateType = "average rate",
            pivotCurrency = CurrencyCode("PLN"),
        )
    }
}
