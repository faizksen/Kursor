package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.RatePoint
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncReport
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncMissingRatesUseCaseTest {

    private var watchlist: List<WatchedPairWithRate> = emptyList()
    private var immediateSyncCalls = 0

    private val ratesRepository = object : RatesRepository {
        override fun observeWatchlist(): Flow<List<WatchedPairWithRate>> = flowOf(watchlist)

        override suspend fun refresh(sourceId: SourceId, only: CurrencyPair?): SyncReport =
            error("not used")
    }

    private val syncScheduler = object : SyncScheduler {
        override fun schedulePeriodicSync() = error("not used")

        override fun requestImmediateSync() {
            immediateSyncCalls++
        }

        override fun requestPairSync(pair: CurrencyPair) = error("not used")

        override fun observeSyncQueue(trigger: SyncTrigger): Flow<SyncQueue> = error("not used")
    }

    private val syncMissingRates = SyncMissingRatesUseCaseImpl(ratesRepository, syncScheduler)

    @Test
    fun theSeededPairsAreFilledOnTheVeryFirstLaunch() = runTest {
        watchlist = listOf(pair("PLN", rate = null), pair("USD", rate = null))

        syncMissingRates()

        assertEquals(1, immediateSyncCalls)
    }

    @Test
    fun aMigrationThatDroppedEveryRateIsRecoveredToo() = runTest {
        watchlist = listOf(pair("PLN", rate = null))

        syncMissingRates()

        assertEquals(1, immediateSyncCalls)
    }

    @Test
    fun aWatchlistThatAlreadyHasNumbersIsLeftAlone() = runTest {
        watchlist = listOf(pair("PLN", rate = ratePoint()), pair("USD", rate = ratePoint()))

        syncMissingRates()

        assertEquals(0, immediateSyncCalls)
    }

    @Test
    fun oneBlankPairAmongFilledOnesIsLeftToItsOwnSync() = runTest {
        watchlist = listOf(pair("PLN", rate = ratePoint()), pair("USD", rate = null))

        syncMissingRates()

        assertEquals(0, immediateSyncCalls)
    }

    @Test
    fun anEmptyWatchlistHasNothingToFetch() = runTest {
        watchlist = emptyList()

        syncMissingRates()

        assertEquals(0, immediateSyncCalls)
    }

    private fun pair(quote: String, rate: RatePoint?) = WatchedPairWithRate(
        watchedPair = WatchedPair(
            id = 1,
            pair = CurrencyPair(base = CurrencyCode("EUR"), quote = CurrencyCode(quote)),
            provider = ProviderKey("ECB"),
            position = 0,
            addedAt = Instant.fromEpochMilliseconds(1_000L),
        ),
        rate = rate,
    )

    private fun ratePoint() = RatePoint(
        date = LocalDate.parse("2026-11-17"),
        value = Rate.parse("4.4016")!!,
        sourceId = SourceId.FRANKFURTER,
        provider = ProviderKey("ECB"),
    )
}
