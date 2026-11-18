package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.WatchedPairsRepository
import dev.faizksen.kursor.data.model.AddPairResult
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.model.WatchedPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveMissingProvidersUseCaseTest {

    private var unresolved: List<WatchedPair> = emptyList()
    private var candidates: List<Provider> = listOf(NBP, ECB)
    private val written = mutableMapOf<Long, ProviderKey>()

    private val watchedPairsRepository = object : WatchedPairsRepository {
        override suspend fun add(pair: CurrencyPair, provider: ProviderKey): AddPairResult =
            error("not used")

        override suspend fun byId(id: Long): WatchedPair? = null

        override suspend fun withoutProvider(): List<WatchedPair> = unresolved

        override suspend fun setProvider(id: Long, provider: ProviderKey) {
            written[id] = provider
        }

        override suspend fun flipDirection(id: Long) = error("not used")

        override suspend fun reorder(ids: List<Long>) = error("not used")

        override suspend fun delete(id: Long): CurrencyPair? = error("not used")

        override suspend fun undoDelete(): Boolean = error("not used")

        override fun forgetDeleted() = error("not used")

        override fun observeDeleted(): Flow<CurrencyPair?> = error("not used")

    }

    private val resolveMissingProviders = ResolveMissingProvidersUseCaseImpl(
        watchedPairsRepository = watchedPairsRepository,
        providerCandidates = { pair ->
            ProviderCandidates(shortlist = candidates, all = candidates)
        },
    )

    @Test
    fun aPairFromBeforeIssuersGetsTheRecommendedOne() = runTest {
        unresolved = listOf(watchedPair(id = 7, quote = "PLN"))

        resolveMissingProviders()

        assertEquals(mapOf(7L to NBP.key), written.toMap())
    }

    @Test
    fun everyUnresolvedPairIsCovered() = runTest {
        unresolved = listOf(watchedPair(id = 1, quote = "PLN"), watchedPair(id = 2, quote = "USD"))

        resolveMissingProviders()

        assertEquals(setOf(1L, 2L), written.keys)
    }

    @Test
    fun aPairNoProviderServesIsLeftAsItIs() = runTest {
        unresolved = listOf(watchedPair(id = 7, quote = "PLN"))
        candidates = emptyList()

        resolveMissingProviders()

        assertEquals(emptyMap(), written.toMap())
    }

    @Test
    fun aWatchlistWithNothingUnresolvedWritesNothing() = runTest {
        unresolved = emptyList()

        resolveMissingProviders()

        assertEquals(emptyMap(), written.toMap())
    }

    private fun watchedPair(id: Long, quote: String) = WatchedPair(
        id = id,
        pair = CurrencyPair(base = CurrencyCode("EUR"), quote = CurrencyCode(quote)),
        provider = null,
        position = 0,
        addedAt = ADDED_AT,
    )

    private companion object {
        val ADDED_AT = Instant.fromEpochMilliseconds(1_000L)
        val NBP = Provider(
            key = ProviderKey("NBP"),
            name = "Narodowy Bank Polski",
            rateType = "average rate",
            pivotCurrency = CurrencyCode("PLN"),
            cadence = PublishCadence.DAILY,
            latestDate = LocalDate.parse("2026-11-17"),
            dataUrl = null,
        )

        val ECB = NBP.copy(
            key = ProviderKey("ECB"),
            name = "European Central Bank",
            pivotCurrency = CurrencyCode("EUR"),
        )
    }
}
