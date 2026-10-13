package dev.faizksen.kursor.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.RatesRemoteSource
import dev.faizksen.kursor.network.model.RemoteRate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.net.UnknownHostException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class RatesRepositoryImplTest {

    private lateinit var database: KursorDatabase
    private lateinit var watchedPairsDao: WatchedPairsDao
    private lateinit var ratesDao: RatesDao

    private val clock = Clock.fixed(
        Instant.parse("2026-09-09T10:00:00Z"),
        ZoneOffset.UTC
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KursorDatabase::class.java,
        ).build()
        watchedPairsDao = database.watchedPairsDao
        ratesDao = database.ratesDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createFrankfurterRemoteSource(remoteRates: List<RemoteRate> = emptyList()) = FakeRatesRemoteSource(
        id = SourceId.FRANKFURTER,
        result = Result.success(remoteRates)
    )

    private fun createRepository(sources: Map<SourceId, RatesRemoteSource>?) = RatesRepositoryImpl(
        ratesDao = ratesDao,
        watchedPairsDao = watchedPairsDao,
        sources = sources ?: emptyMap(),
        clock = clock,
    )

    private suspend fun putWatchedPairsIntoDatabase() {
        watchedPairsDao.insertAtEnd(EUR, USD, 1_000L)
        watchedPairsDao.insertAtEnd(USD, PLN, 1_000L)
    }

    @Test
    fun refreshWithoutWatchedPairs() = runTest {
        val fakeSource = createFrankfurterRemoteSource()
        val repository = createRepository(mapOf(fakeSource.id to fakeSource))
        val result = repository.refresh(fakeSource.id)
        assertThat(result).isEqualTo(SyncOutcome.Success(0))
        assertThat(fakeSource.callCount).isEqualTo(0)
    }

    @Test
    fun refreshWithWatchedPairs() = runTest {
        putWatchedPairsIntoDatabase()
        val fakeSource = createFrankfurterRemoteSource(remoteRates)
        val repository = createRepository(mapOf(fakeSource.id to fakeSource))
        val result = repository.refresh(fakeSource.id)
        assertThat(result).isEqualTo(SyncOutcome.Success(2))
        val rateEntities = ratesDao.observeLatestRates().first()
        val fetchedAt = clock.millis()
        assertThat(rateEntities).containsExactlyElementsIn(
            listOf(
                RateEntity(
                    base = EUR,
                    quote = USD,
                    date = DATE,
                    sourceId = SourceIdColumn.Frankfurter,
                    value = RATE_EUR_USD,
                    fetchedAt = fetchedAt
                ),
                RateEntity(
                    base = USD,
                    quote = PLN,
                    date = DATE,
                    sourceId = SourceIdColumn.Frankfurter,
                    value = RATE_USD_PLN,
                    fetchedAt = fetchedAt
                ),
            )
        )
    }

    @Test
    fun refreshWithoutSource() = runTest {
        putWatchedPairsIntoDatabase()
        val repository = createRepository(null)
        val result = repository.refresh(SourceId.FRANKFURTER)
        assertThat(result).isEqualTo(SyncOutcome.Permanent(PermanentKind.UnknownSource))
    }

    @Test
    fun refreshSourceError() = runTest {
        putWatchedPairsIntoDatabase()
        val sourceId = SourceId.FRANKFURTER
        val fakeSource = FakeRatesRemoteSource(
            id = sourceId,
            result = Result.failure(UnknownHostException())
        )
        val repository = createRepository(mapOf(sourceId to fakeSource))
        val result = repository.refresh(sourceId)
        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.NoNetwork))

        val rateEntities = ratesDao.observeLatestRates().first()
        assertThat(rateEntities).isEmpty()
    }

    @Test
    fun observeWatchListPairWithoutRate() = runTest {
        putWatchedPairsIntoDatabase()

        val fakeSource = createFrankfurterRemoteSource()
        val sources = mapOf(fakeSource.id to fakeSource)
        val repository = createRepository(sources)

        repository.refresh(fakeSource.id)

        val results = repository.observeWatchlist().first()

        assertThat(results).hasSize(2)
        assertThat(results[0].rate).isNull()
        assertThat(results[1].rate).isNull()
    }

    @Test
    fun observeWatchListPairWithRate() = runTest {
        putWatchedPairsIntoDatabase()

        val fakeSource = createFrankfurterRemoteSource(remoteRates)
        val sources = mapOf(fakeSource.id to fakeSource)
        val repository = createRepository(sources)

        repository.refresh(fakeSource.id)

        val results = repository.observeWatchlist().first()

        assertThat(results).hasSize(2)
        val eurUsdPair = results[0]
        assertThat(eurUsdPair.rate).isNotNull()
        assertThat(eurUsdPair.rate!!.date).isEqualTo(LocalDate.parse(DATE))
        assertThat(eurUsdPair.rate!!.value.compareTo(BigDecimal(RATE_EUR_USD))).isEqualTo(0)
        assertThat(eurUsdPair.rate!!.sourceId).isEqualTo(SourceId.FRANKFURTER)

        val usdPlnPair = results[1]
        assertThat(usdPlnPair).isNotNull()
        assertThat(usdPlnPair.rate).isNotNull()
        assertThat(usdPlnPair.rate!!.date).isEqualTo(LocalDate.parse(DATE))
        assertThat(usdPlnPair.rate!!.value.compareTo(BigDecimal(RATE_USD_PLN))).isEqualTo(0)
        assertThat(usdPlnPair.rate!!.sourceId).isEqualTo(SourceId.FRANKFURTER)
    }

    @Test
    fun observeWatchListOnePairWithTwoSources() = runTest {
        watchedPairsDao.insertAtEnd(EUR, USD, 1_000L)

        val firstSource = createFrankfurterRemoteSource(remoteRates)
        val secondSource = FakeRatesRemoteSource(
            id = SourceId.CBR,
            result = Result.success(
                listOf(
                    RemoteRate(
                        base = CurrencyCode(EUR),
                        quote = CurrencyCode(USD),
                        date = LocalDate.parse(DATE),
                        value = BigDecimal(RATE_EUR_USD_CBR)
                    )
                )
            )
        )
        val sources = mapOf(
            firstSource.id to firstSource,
            secondSource.id to secondSource
        )
        val repository = createRepository(sources)

        repository.refresh(firstSource.id)
        repository.refresh(secondSource.id)

        val results = repository.observeWatchlist().first().mapNotNull { it.rate }
        assertThat(results).hasSize(1)
        assertThat(results[0].sourceId).isEqualTo(SourceId.FRANKFURTER)
        assertThat(results[0].value).isEqualTo(BigDecimal(RATE_EUR_USD))
        assertThat(results[0].value.compareTo(BigDecimal(RATE_EUR_USD))).isEqualTo(0)
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
                    fetchedAt = clock.millis()
                )
            )
        )

        val fakeSource = createFrankfurterRemoteSource()
        val repository = createRepository(mapOf(fakeSource.id to fakeSource))

        val results = repository.observeWatchlist().first()

        assertThat(results).hasSize(2)
        assertThat(results[0].rate).isNull()
        assertThat(results[1].rate).isNull()
    }

    companion object {
        private const val EUR = "EUR"
        private const val USD = "USD"
        private const val PLN = "PLN"
        private const val DATE = "2026-09-09"
        private const val RATE_EUR_USD = "1.24"
        private const val RATE_EUR_USD_CBR = "1.26"
        private const val RATE_USD_PLN = "0.24"
        private val remoteRates = listOf(
            RemoteRate(
                CurrencyCode("EUR"),
                CurrencyCode("USD"),
                LocalDate.parse(DATE),
                BigDecimal(RATE_EUR_USD)
            ),
            RemoteRate(
                CurrencyCode("USD"),
                CurrencyCode("PLN"),
                LocalDate.parse(DATE),
                BigDecimal(RATE_USD_PLN)
            ),
        )
    }
}