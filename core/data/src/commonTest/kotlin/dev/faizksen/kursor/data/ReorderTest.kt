package dev.faizksen.kursor.data

import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class ReorderTest {

    private lateinit var database: KursorDatabase
    private lateinit var repository: WatchedPairsRepository

    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-11-18T09:00:00Z")
    }

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        repository = WatchedPairsRepositoryImpl(
            watchedPairsDao = database.watchedPairsDao,
            ratesDao = database.ratesDao,
            clock = clock,
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun pairsStartOutInTheOrderTheyWereAdded() = runTest {
        val ids = addThree()

        assertEquals(listOf(0, 1, 2), ids.map { id -> repository.byId(id)?.position })
    }

    @Test
    fun theOrderTheUserChoosesIsTheOrderThatIsKept() = runTest {
        val (first, second, third) = addThree()

        repository.reorder(listOf(third, first, second))

        assertEquals(0, repository.byId(third)?.position)
        assertEquals(1, repository.byId(first)?.position)
        assertEquals(2, repository.byId(second)?.position)
    }

    @Test
    fun reorderingTwiceLeavesNoGapsOrDuplicates() = runTest {
        val (first, second, third) = addThree()

        repository.reorder(listOf(third, second, first))
        repository.reorder(listOf(second, first, third))

        val positions = listOf(first, second, third).map { id -> repository.byId(id)?.position }

        assertEquals(listOf(1, 0, 2), positions)
    }

    @Test
    fun aPairAddedAfterAReorderGoesToTheEnd() = runTest {
        val (first, second, third) = addThree()

        repository.reorder(listOf(third, second, first))
        val fourth = repository.add(pair("EUR", "CZK"), ProviderKey("CNB")).id

        assertEquals(3, repository.byId(fourth)?.position)
    }

    private suspend fun addThree(): List<Long> = listOf(
        repository.add(pair("EUR", "PLN"), ProviderKey("NBP")).id,
        repository.add(pair("EUR", "USD"), ProviderKey("ECB")).id,
        repository.add(pair("EUR", "RUB"), ProviderKey("CBR")).id,
    )

    private fun pair(base: String, quote: String) =
        CurrencyPair(CurrencyCode(base), CurrencyCode(quote))
}
