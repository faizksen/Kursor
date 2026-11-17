package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.WatchedPairsRepository
import dev.faizksen.kursor.data.model.AddPairResult
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.model.WatchedPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddWatchedPairUseCaseTest {

    private var result: AddPairResult = AddPairResult.Added(1)
    private var addedPair: CurrencyPair? = null
    private var addedProvider: ProviderKey? = null
    private var syncedPair: CurrencyPair? = null
    private var immediateSyncCalls = 0

    private val watchedPairsRepository = object : WatchedPairsRepository {
        override suspend fun add(pair: CurrencyPair, provider: ProviderKey): AddPairResult {
            addedPair = pair
            addedProvider = provider
            return result
        }

        override suspend fun withoutProvider(): List<WatchedPair> = emptyList()

        override suspend fun setProvider(id: Long, provider: ProviderKey) = error("not used")
    }

    private val syncScheduler = object : SyncScheduler {
        override fun schedulePeriodicSync() = error("not used")

        override fun requestImmediateSync() {
            immediateSyncCalls++
        }

        override fun requestPairSync(pair: CurrencyPair) {
            syncedPair = pair
        }

        override fun observeSyncQueue(trigger: SyncTrigger): Flow<SyncQueue> = error("not used")
    }

    private val addWatchedPair = AddWatchedPairUseCaseImpl(watchedPairsRepository, syncScheduler)

    @Test
    fun addingAPairAsksForItsRatesStraightAway() = runTest {
        val outcome = addWatchedPair(EUR_PLN, NBP)

        assertEquals(AddPairOutcome.ADDED, outcome)
        assertEquals(EUR_PLN, addedPair)
        assertEquals(NBP, addedProvider)
        assertEquals(EUR_PLN, syncedPair)
    }

    @Test
    fun onlyTheNewPairIsRequested() = runTest {
        addWatchedPair(EUR_PLN, NBP)

        assertEquals(0, immediateSyncCalls)
    }

    @Test
    fun aPairAlreadyWatchedThroughThatProviderStartsNoSync() = runTest {
        result = AddPairResult.AlreadyWatched(7)

        val outcome = addWatchedPair(EUR_PLN, NBP)

        assertEquals(AddPairOutcome.ALREADY_WATCHED, outcome)
        assertNull(syncedPair)
    }

    private companion object {
        val EUR_PLN = CurrencyPair(base = CurrencyCode("EUR"), quote = CurrencyCode("PLN"))
        val NBP = ProviderKey("NBP")
    }
}
