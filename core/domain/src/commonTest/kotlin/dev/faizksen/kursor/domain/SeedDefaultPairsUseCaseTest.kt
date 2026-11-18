package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.AppStateRepository
import dev.faizksen.kursor.data.WatchedPairsRepository
import dev.faizksen.kursor.data.model.AddPairResult
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.WatchedPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeedDefaultPairsUseCaseTest {

    private var seeded = false
    private val added = mutableListOf<Pair<CurrencyPair, ProviderKey>>()

    private val watchedPairsRepository = object : WatchedPairsRepository {
        override suspend fun add(pair: CurrencyPair, provider: ProviderKey): AddPairResult {
            added += pair to provider
            return AddPairResult.Added(added.size.toLong())
        }

        override suspend fun byId(id: Long): WatchedPair? = null

        override suspend fun withoutProvider(): List<WatchedPair> = error("not used")

        override suspend fun setProvider(id: Long, provider: ProviderKey) = error("not used")

        override suspend fun delete(id: Long): CurrencyPair? = error("not used")

        override suspend fun undoDelete(): Boolean = error("not used")

        override fun forgetDeleted() = error("not used")

        override fun observeDeleted(): Flow<CurrencyPair?> = error("not used")

    }

    private val appStateRepository = object : AppStateRepository {
        override suspend fun defaultsSeeded(): Boolean = seeded

        override fun observeDefaultsSeeded(): Flow<Boolean> = flowOf(seeded)

        override suspend fun markDefaultsSeeded() {
            seeded = true
        }
    }

    private val seedDefaultPairs = SeedDefaultPairsUseCaseImpl(
        watchedPairsRepository = watchedPairsRepository,
        appStateRepository = appStateRepository,
    )

    @Test
    fun theFirstLaunchGetsAWatchlistThatIsNotEmpty() = runTest {
        seedDefaultPairs()

        assertEquals(DEFAULT_PAIRS, added)
    }

    @Test
    fun eachSeededPairComesWithTheIssuerItShouldFollow() = runTest {
        seedDefaultPairs()

        assertEquals(
            listOf("NBP", "ECB", "CBR"),
            added.map { (_, provider) -> provider.key },
        )
    }

    @Test
    fun theSecondLaunchAddsNothing() = runTest {
        seedDefaultPairs()
        added.clear()

        seedDefaultPairs()

        assertTrue(added.isEmpty())
    }

    @Test
    fun aWatchlistEmptiedOnPurposeStaysEmpty() = runTest {
        seeded = true

        seedDefaultPairs()

        assertTrue(added.isEmpty())
    }

    @Test
    fun theFlagIsOnlySetOnceThePairsAreIn() = runTest {
        assertFalse(seeded)

        seedDefaultPairs()

        assertTrue(seeded)
    }
}
