package dev.faizksen.kursor.data

import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.network.model.RemoteRate
import dev.faizksen.kursor.network.model.RemoteSourceException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class RefreshPerProviderTest {

    private lateinit var database: KursorDatabase
    private lateinit var ratesDao: RatesDao
    private lateinit var watchedPairsDao: WatchedPairsDao

    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(5_000L)
    }

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        ratesDao = database.ratesDao
        watchedPairsDao = database.watchedPairsDao
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun eachIssuerIsAskedThroughItsOwnCall() = runTest {
        watchPairs()
        val source = FakeRatesRemoteSource(
            perProvider = mapOf(
                NBP to Result.success(listOf(remote("PLN", "4.4069"))),
                ECB to Result.success(listOf(remote("USD", "1.1014"))),
            )
        )
        val repository = repository(source)

        val outcome = repository.refresh(SourceId.FRANKFURTER)

        assertEquals(SyncOutcome.Success(2, 2), outcome)
        assertEquals(setOf<ProviderKey?>(NBP, ECB), source.askedProviders.toSet())
    }

    @Test
    fun theRateIsStoredUnderTheIssuerItCameFrom() = runTest {
        watchPairs()
        val repository = repository(
            FakeRatesRemoteSource(
                perProvider = mapOf(
                    NBP to Result.success(listOf(remote("PLN", "4.4069"))),
                    ECB to Result.success(listOf(remote("USD", "1.1014"))),
                )
            )
        )

        repository.refresh(SourceId.FRANKFURTER)

        assertEquals(
            setOf("PLN" to "NBP", "USD" to "ECB"),
            ratesDao.observeLatestRates().first().map { it.quote to it.provider }.toSet(),
        )
    }

    @Test
    fun aPairOnlySeesTheRateOfItsOwnIssuer() = runTest {
        watchedPairsDao.addAtEnd(EUR, "PLN", NBP.key, 1_000L)
        watchedPairsDao.addAtEnd(EUR, "PLN", ECB.key, 1_000L)
        val repository = repository(
            FakeRatesRemoteSource(
                perProvider = mapOf(
                    NBP to Result.success(listOf(remote("PLN", "4.4069"))),
                    ECB to Result.success(listOf(remote("PLN", "4.4113"))),
                )
            )
        )

        repository.refresh(SourceId.FRANKFURTER)

        assertEquals(
            listOf("4.4069", "4.4113"),
            repository.observeWatchlist().first().map { it.rate?.value?.toPlainString() },
        )
    }

    @Test
    fun whatCameBackIsKeptWhenAnIssuerFails() = runTest {
        watchPairs()
        val repository = repository(
            FakeRatesRemoteSource(
                perProvider = mapOf(
                    NBP to Result.success(listOf(remote("PLN", "4.4069"))),
                    ECB to Result.failure(
                        RemoteSourceException.ServerError(500, RuntimeException())
                    ),
                )
            )
        )

        val outcome = repository.refresh(SourceId.FRANKFURTER)

        assertEquals(SyncOutcome.Success(1, 2), outcome)
        assertEquals(listOf("PLN"), ratesDao.observeLatestRates().first().map { it.quote })
    }

    @Test
    fun aRunThatFetchedNothingIsReportedForRetry() = runTest {
        watchPairs()
        val repository = repository(
            FakeRatesRemoteSource(
                result = Result.failure(RemoteSourceException.ServerError(500, RuntimeException()))
            )
        )

        val outcome = repository.refresh(SourceId.FRANKFURTER)

        assertEquals(SyncOutcome.Transient(TransientKind.ServerError), outcome)
    }

    @Test
    fun anIssuerThatAnswersWithNothingIsNotAFailure() = runTest {
        watchPairs()
        val repository = repository(
            FakeRatesRemoteSource(
                perProvider = mapOf(
                    NBP to Result.success(listOf(remote("PLN", "4.4069"))),
                    ECB to Result.success(emptyList()),
                )
            )
        )

        val outcome = repository.refresh(SourceId.FRANKFURTER)

        assertEquals(SyncOutcome.Success(1, 2), outcome)
        assertNull(
            repository.observeWatchlist()
                .first()
                .single { watched -> watched.watchedPair.pair.quote.code == "USD" }
                .rate
        )
    }

    @Test
    fun oneRequestedPairAsksOnlyItsOwnIssuer() = runTest {
        watchPairs()
        val source = FakeRatesRemoteSource(
            perProvider = mapOf(NBP to Result.success(listOf(remote("PLN", "4.4069"))))
        )
        val repository = repository(source)

        repository.refresh(
            sourceId = SourceId.FRANKFURTER,
            only = CurrencyPair(CurrencyCode(EUR), CurrencyCode("PLN")),
        )

        assertEquals(listOf<ProviderKey?>(NBP), source.askedProviders)
    }

    private suspend fun watchPairs() {
        watchedPairsDao.addAtEnd(EUR, "PLN", NBP.key, 1_000L)
        watchedPairsDao.addAtEnd(EUR, "USD", ECB.key, 1_000L)
    }

    private fun repository(source: FakeRatesRemoteSource) = RatesRepositoryImpl(
        ratesDao = ratesDao,
        watchedPairsDao = watchedPairsDao,
        sources = mapOf(source.id to source),
        clock = clock,
    )

    private fun remote(quote: String, value: String) = RemoteRate(
        base = CurrencyCode(EUR),
        quote = CurrencyCode(quote),
        date = LocalDate.parse("2026-11-17"),
        value = Rate.parse(value)!!,
    )

    private companion object {
        const val EUR = "EUR"
        val NBP = ProviderKey("NBP")
        val ECB = ProviderKey("ECB")
    }
}
