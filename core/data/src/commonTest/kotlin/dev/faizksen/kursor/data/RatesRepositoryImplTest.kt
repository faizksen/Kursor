package dev.faizksen.kursor.data

import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.network.RatesRemoteSource
import dev.faizksen.kursor.network.model.RemoteRate
import dev.faizksen.kursor.network.model.RemoteSourceException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class RatesRepositoryImplTest {

    private lateinit var database: KursorDatabase
    private lateinit var watchedPairsDao: WatchedPairsDao
    private lateinit var ratesDao: RatesDao

    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-09-09T10:00:00Z")
    }

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        watchedPairsDao = database.watchedPairsDao
        ratesDao = database.ratesDao
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun refreshWithoutWatchedPairs() = runTest {
        val fakeSource = frankfurterSource()
        val repository = repository(mapOf(fakeSource.id to fakeSource))

        val result = repository.refresh(fakeSource.id)

        assertEquals(SyncOutcome.Success(0), result)
        assertEquals(0, fakeSource.callCount)
    }

    @Test
    fun refreshWithWatchedPairs() = runTest {
        putWatchedPairsIntoDatabase()
        val fakeSource = frankfurterSource(REMOTE_RATES)
        val repository = repository(mapOf(fakeSource.id to fakeSource))

        val result = repository.refresh(fakeSource.id)

        assertEquals(SyncOutcome.Success(2), result)

        val fetchedAt = clock.now().toEpochMilliseconds()
        assertEquals(
            setOf(
                RateEntity(EUR, USD, DATE, SourceIdColumn.Frankfurter, RATE_EUR_USD, fetchedAt),
                RateEntity(USD, PLN, DATE, SourceIdColumn.Frankfurter, RATE_USD_PLN, fetchedAt),
            ),
            ratesDao.observeLatestRates().first().toSet(),
        )
    }

    @Test
    fun refreshWithoutSource() = runTest {
        putWatchedPairsIntoDatabase()
        val repository = repository(emptyMap())

        val result = repository.refresh(SourceId.FRANKFURTER)

        assertEquals(SyncOutcome.Permanent(PermanentKind.UnknownSource), result)
    }

    @Test
    fun refreshSourceError() = runTest {
        putWatchedPairsIntoDatabase()
        val fakeSource = FakeRatesRemoteSource(
            id = SourceId.FRANKFURTER,
            result = Result.failure(RemoteSourceException.NoNetwork(cause = null)),
        )
        val repository = repository(mapOf(fakeSource.id to fakeSource))

        val result = repository.refresh(fakeSource.id)

        assertEquals(SyncOutcome.Transient(TransientKind.NoNetwork), result)
        assertEquals(0, ratesDao.observeLatestRates().first().size)
    }

    @Test
    fun observeWatchListPairWithoutRate() = runTest {
        putWatchedPairsIntoDatabase()
        val fakeSource = frankfurterSource()
        val repository = repository(mapOf(fakeSource.id to fakeSource))

        repository.refresh(fakeSource.id)
        val results = repository.observeWatchlist().first()

        assertEquals(2, results.size)
        assertNull(results[0].rate)
        assertNull(results[1].rate)
    }

    @Test
    fun observeWatchListPairWithRate() = runTest {
        putWatchedPairsIntoDatabase()
        val fakeSource = frankfurterSource(REMOTE_RATES)
        val repository = repository(mapOf(fakeSource.id to fakeSource))

        repository.refresh(fakeSource.id)
        val results = repository.observeWatchlist().first()

        assertEquals(2, results.size)

        val eurUsd = assertNotNull(results[0].rate)
        assertEquals(LocalDate.parse(DATE), eurUsd.date)
        assertEquals(Rate.parse(RATE_EUR_USD), eurUsd.value)
        assertEquals(SourceId.FRANKFURTER, eurUsd.sourceId)

        val usdPln = assertNotNull(results[1].rate)
        assertEquals(LocalDate.parse(DATE), usdPln.date)
        assertEquals(Rate.parse(RATE_USD_PLN), usdPln.value)
        assertEquals(SourceId.FRANKFURTER, usdPln.sourceId)
    }

    @Test
    fun observeWatchListOnePairWithTwoSources() = runTest {
        watchedPairsDao.insertAtEnd(EUR, USD, 1_000L)

        val frankfurter = frankfurterSource(REMOTE_RATES)
        val cbr = FakeRatesRemoteSource(
            id = SourceId.CBR,
            result = Result.success(
                listOf(
                    RemoteRate(
                        base = CurrencyCode(EUR),
                        quote = CurrencyCode(USD),
                        date = LocalDate.parse(DATE),
                        value = Rate.parse(RATE_EUR_USD_CBR)!!,
                    )
                )
            ),
        )
        val repository = repository(mapOf(frankfurter.id to frankfurter, cbr.id to cbr))

        repository.refresh(frankfurter.id)
        repository.refresh(cbr.id)

        val results = repository.observeWatchlist().first().mapNotNull { it.rate }

        assertEquals(1, results.size)
        assertEquals(SourceId.FRANKFURTER, results[0].sourceId)
        assertEquals(Rate.parse(RATE_EUR_USD), results[0].value)
    }

    @Test
    fun observeWatchListUnknownSourceIdInDataBase() = runTest {
        putWatchedPairsIntoDatabase()
        ratesDao.upsertAll(
            listOf(
                RateEntity(
                    base = EUR,
                    quote = USD,
                    date = DATE,
                    sourceId = SourceIdColumn.Unrecognized("UNKNOWN_SOURCE"),
                    value = RATE_EUR_USD,
                    fetchedAt = clock.now().toEpochMilliseconds(),
                )
            )
        )
        val fakeSource = frankfurterSource()
        val repository = repository(mapOf(fakeSource.id to fakeSource))

        val results = repository.observeWatchlist().first()

        assertEquals(2, results.size)
        assertNull(results[0].rate)
        assertNull(results[1].rate)
    }

    private fun frankfurterSource(remoteRates: List<RemoteRate> = emptyList()) = FakeRatesRemoteSource(
        id = SourceId.FRANKFURTER,
        result = Result.success(remoteRates),
    )

    private fun repository(sources: Map<SourceId, RatesRemoteSource>) = RatesRepositoryImpl(
        ratesDao = ratesDao,
        watchedPairsDao = watchedPairsDao,
        sources = sources,
        clock = clock,
    )

    private suspend fun putWatchedPairsIntoDatabase() {
        watchedPairsDao.insertAtEnd(EUR, USD, 1_000L)
        watchedPairsDao.insertAtEnd(USD, PLN, 1_000L)
    }

    private companion object {
        const val EUR = "EUR"
        const val USD = "USD"
        const val PLN = "PLN"
        const val DATE = "2026-09-09"
        const val RATE_EUR_USD = "1.24"
        const val RATE_EUR_USD_CBR = "1.26"
        const val RATE_USD_PLN = "0.24"

        val REMOTE_RATES = listOf(
            RemoteRate(
                CurrencyCode(EUR),
                CurrencyCode(USD),
                LocalDate.parse(DATE),
                Rate.parse(RATE_EUR_USD)!!,
            ),
            RemoteRate(
                CurrencyCode(USD),
                CurrencyCode(PLN),
                LocalDate.parse(DATE),
                Rate.parse(RATE_USD_PLN)!!,
            ),
        )
    }
}
