package dev.faizksen.kursor.data

import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.model.Catalogue
import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.CurrencyType
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderCoverage
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class CatalogueRepositoryTest {

    private lateinit var database: KursorDatabase
    private lateinit var remoteSource: FakeCatalogueRemoteSource
    private lateinit var repository: CatalogueRepository

    private var now: Instant = Instant.fromEpochMilliseconds(1_000L)

    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        remoteSource = FakeCatalogueRemoteSource(CATALOGUE)
        repository = CatalogueRepositoryImpl(
            remoteSource = remoteSource,
            catalogueDao = database.catalogueDao,
            clock = clock,
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun nothingIsStoredBeforeTheFirstRefresh() = runTest {
        assertNull(repository.fetchedAt())
        assertEquals(emptyList(), repository.fiatCurrencies())
    }

    @Test
    fun refreshStoresTheCatalogueWithTheClocksTime() = runTest {
        now = Instant.fromEpochMilliseconds(5_000L)

        repository.refresh()

        assertEquals(Instant.fromEpochMilliseconds(5_000L), repository.fetchedAt())
    }

    @Test
    fun onlyFiatReachesThePicker() = runTest {
        repository.refresh()

        assertEquals(
            listOf(
                CurrencyCode("CHF"),
                CurrencyCode("EUR"),
                CurrencyCode("PLN"),
                CurrencyCode("UAH"),
            ),
            repository.fiatCurrencies().map { currency -> currency.code },
        )
    }

    @Test
    fun aCurrencyIsReadBackAsItWasFetched() = runTest {
        repository.refresh()

        assertEquals(
            Currency(
                code = CurrencyCode("PLN"),
                name = "Polish Zloty",
                symbol = "zl",
                type = CurrencyType.FIAT,
            ),
            repository.fiatCurrencies().single { currency -> currency.code == CurrencyCode("PLN") },
        )
    }

    @Test
    fun onlyProvidersServingBothCurrenciesAreCandidates() = runTest {
        repository.refresh()

        assertEquals(
            setOf(ProviderKey("ECB"), ProviderKey("NBP")),
            repository.providersFor(pair("EUR", "PLN")).map { it.key }.toSet(),
        )
        assertEquals(
            setOf(ProviderKey("ECB")),
            repository.providersFor(pair("EUR", "CHF")).map { it.key }.toSet(),
        )
        assertEquals(
            setOf(ProviderKey("ECB")),
            repository.providersFor(pair("PLN", "CHF")).map { it.key }.toSet(),
        )
        assertEquals(
            emptySet(),
            repository.providersFor(pair("UAH", "CHF")).map { it.key }.toSet(),
        )
    }

    @Test
    fun aProviderIsReadBackWithTheMetadataTheRankingNeeds() = runTest {
        repository.refresh()

        assertEquals(
            Provider(
                key = ProviderKey("NBP"),
                name = "Narodowy Bank Polski",
                rateType = "official rate",
                pivotCurrency = CurrencyCode("PLN"),
                cadence = PublishCadence.DAILY,
                latestDate = LocalDate.parse("2026-11-17"),
                dataUrl = null,
            ),
            repository.providersFor(pair("EUR", "PLN")).single { it.key == ProviderKey("NBP") },
        )
    }

    @Test
    fun refreshReplacesTheEarlierCatalogueInsteadOfAddingToIt() = runTest {
        repository.refresh()

        remoteSource.serve(
            Catalogue(
                currencies = listOf(fiat("PLN", "Polish Zloty", "zl")),
                providers = listOf(NBP),
                coverage = listOf(
                    ProviderCoverage(ProviderKey("NBP"), CurrencyCode("PLN")),
                    ProviderCoverage(ProviderKey("NBP"), CurrencyCode("EUR")),
                ),
            )
        )
        now = Instant.fromEpochMilliseconds(9_000L)
        repository.refresh()

        assertEquals(2, remoteSource.fetchCount)
        assertEquals(Instant.fromEpochMilliseconds(9_000L), repository.fetchedAt())
        assertEquals(
            listOf(CurrencyCode("PLN")),
            repository.fiatCurrencies().map { currency -> currency.code },
        )
        assertEquals(
            setOf(ProviderKey("NBP")),
            repository.providersFor(pair("EUR", "PLN")).map { it.key }.toSet(),
        )
    }

    private fun pair(base: String, quote: String) =
        CurrencyPair(base = CurrencyCode(base), quote = CurrencyCode(quote))

    private companion object {
        fun fiat(code: String, name: String, symbol: String) = Currency(
            code = CurrencyCode(code),
            name = name,
            symbol = symbol,
            type = CurrencyType.FIAT,
        )

        val NBP = Provider(
            key = ProviderKey("NBP"),
            name = "Narodowy Bank Polski",
            rateType = "official rate",
            pivotCurrency = CurrencyCode("PLN"),
            cadence = PublishCadence.DAILY,
            latestDate = LocalDate.parse("2026-11-17"),
            dataUrl = null,
        )

        val ECB = Provider(
            key = ProviderKey("ECB"),
            name = "European Central Bank",
            rateType = "reference rate",
            pivotCurrency = CurrencyCode("EUR"),
            cadence = PublishCadence.DAILY,
            latestDate = LocalDate.parse("2026-11-17"),
            dataUrl = null,
        )

        val CATALOGUE = Catalogue(
            currencies = listOf(
                fiat("PLN", "Polish Zloty", "zl"),
                fiat("EUR", "Euro", "E"),
                fiat("CHF", "Swiss Franc", "Fr"),
                fiat("UAH", "Ukrainian Hryvnia", "H"),
                Currency(CurrencyCode("BTC"), "Bitcoin", "B", CurrencyType.CRYPTO),
                Currency(CurrencyCode("XAU"), "Gold", "oz", CurrencyType.METAL),
            ),
            providers = listOf(NBP, ECB),
            coverage = listOf(
                ProviderCoverage(ProviderKey("NBP"), CurrencyCode("PLN")),
                ProviderCoverage(ProviderKey("NBP"), CurrencyCode("EUR")),
                ProviderCoverage(ProviderKey("NBP"), CurrencyCode("UAH")),
                ProviderCoverage(ProviderKey("ECB"), CurrencyCode("PLN")),
                ProviderCoverage(ProviderKey("ECB"), CurrencyCode("CHF")),
                ProviderCoverage(ProviderKey("ECB"), CurrencyCode("EUR")),
            ),
        )
    }
}
