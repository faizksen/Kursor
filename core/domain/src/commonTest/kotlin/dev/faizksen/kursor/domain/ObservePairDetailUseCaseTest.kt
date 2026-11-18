package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.CatalogueRepository
import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ObservePairDetailUseCaseTest {

    private var watchlist: List<WatchedPairWithRate> = listOf(watched(id = 1))
    private var askedFor: List<ProviderKey> = emptyList()

    private val catalogueRepository = object : CatalogueRepository {
        override suspend fun fetchedAt(): Instant? = error("not used")

        override suspend fun refresh() = error("not used")

        override suspend fun fiatCurrencies(): List<Currency> = error("not used")

        override suspend fun providersFor(pair: CurrencyPair): List<Provider> = error("not used")

        override suspend fun providers(keys: List<ProviderKey>): List<Provider> {
            askedFor = keys
            return listOf(NBP).filter { provider -> provider.key in keys }
        }
    }

    private val observePairDetail = ObservePairDetailUseCaseImpl(
        observeWatchlist = { flowOf(watchlist) },
        catalogueRepository = catalogueRepository,
    )

    @Test
    fun thePairTheScreenWasOpenedForIsTheOneItGets() = runTest {
        watchlist = listOf(watched(id = 1), watched(id = 2, quote = "USD"))

        val detail = observePairDetail(2).first()

        assertEquals(CurrencyCode("USD"), detail?.watched?.watchedPair?.pair?.quote)
    }

    @Test
    fun theIssuerComesWithItsFullName() = runTest {
        val detail = observePairDetail(1).first()

        assertEquals("Narodowy Bank Polski", detail?.provider?.name)
        assertEquals(listOf(NBP.key), askedFor)
    }

    @Test
    fun aPairThatIsNotOnTheListReadsAsGone() = runTest {
        assertNull(observePairDetail(404).first())
    }

    @Test
    fun aPairWithoutAnIssuerAsksTheCatalogueNothing() = runTest {
        watchlist = listOf(watched(id = 1, provider = null))

        val detail = observePairDetail(1).first()

        assertNull(detail?.provider)
        assertTrue(askedFor.isEmpty())
    }

    @Test
    fun theQuietFlagIsCarriedThroughRatherThanRecomputed() = runTest {
        watchlist = listOf(watched(id = 1).copy(quiet = true))

        assertEquals(true, observePairDetail(1).first()?.watched?.quiet)
    }

    private companion object {
        val NBP = Provider(
            key = ProviderKey("NBP"),
            name = "Narodowy Bank Polski",
            rateType = "official",
            pivotCurrency = CurrencyCode("PLN"),
            cadence = PublishCadence.DAILY,
            latestDate = LocalDate.parse("2026-11-17"),
            dataUrl = null,
        )

        fun watched(
            id: Long,
            quote: String = "PLN",
            provider: ProviderKey? = ProviderKey("NBP"),
        ) = WatchedPairWithRate(
            watchedPair = WatchedPair(
                id = id,
                pair = CurrencyPair(CurrencyCode("EUR"), CurrencyCode(quote)),
                provider = provider,
                position = 0,
                addedAt = Instant.parse("2026-11-01T00:00:00Z"),
            ),
            rate = RatePoint(
                date = LocalDate.parse("2026-11-17"),
                value = Rate.parse("4.4032")!!,
                sourceId = SourceId.FRANKFURTER,
                provider = provider,
            ),
        )
    }
}
