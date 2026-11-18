package dev.faizksen.kursor.data

import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class DeleteAndUndoTest {

    private lateinit var database: KursorDatabase
    private lateinit var watchedPairsDao: WatchedPairsDao
    private lateinit var ratesDao: RatesDao
    private lateinit var repository: WatchedPairsRepository

    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-11-18T08:00:00Z")
    }

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        watchedPairsDao = database.watchedPairsDao
        ratesDao = database.ratesDao
        repository = WatchedPairsRepositoryImpl(
            watchedPairsDao = watchedPairsDao,
            ratesDao = ratesDao,
            clock = clock,
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun deletingAPairTakesItsRatesWithIt() = runTest {
        val id = addEurPln()

        repository.delete(id)

        assertNull(repository.byId(id))
        assertTrue(ratesDao.ratesOf("EUR", "PLN", "NBP").isEmpty())
    }

    @Test
    fun anotherPairsRatesSurviveTheDeletion() = runTest {
        val id = addEurPln()
        addRate(base = "EUR", quote = "USD", provider = "ECB")

        repository.delete(id)

        assertEquals(1, ratesDao.ratesOf("EUR", "USD", "ECB").size)
    }

    @Test
    fun undoPutsBackThePairAndEveryRateItHad() = runTest {
        val id = addEurPln()
        addRate(date = "2026-11-16")

        repository.delete(id)
        repository.undoDelete()

        assertNotNull(repository.byId(id))
        assertEquals(2, ratesDao.ratesOf("EUR", "PLN", "NBP").size)
    }

    @Test
    fun undoKeepsTheIdSoAnOpenScreenStillPointsAtIt() = runTest {
        val id = addEurPln()

        repository.delete(id)
        repository.undoDelete()

        assertEquals(id, repository.byId(id)?.id)
    }

    @Test
    fun undoKeepsThePairWhereItWasInTheList() = runTest {
        addEurPln()
        val second = repository.add(pair("EUR", "USD"), ProviderKey("ECB")).id

        repository.delete(second)
        repository.undoDelete()

        assertEquals(1, repository.byId(second)?.position)
    }

    @Test
    fun theDeletedPairIsOfferedForUndoAndThenIsNot() = runTest {
        val id = addEurPln()

        repository.delete(id)
        assertEquals(pair("EUR", "PLN"), repository.observeDeleted().first())

        repository.undoDelete()
        assertNull(repository.observeDeleted().first())
    }

    @Test
    fun lettingTheOfferLapseLeavesThePairDeleted() = runTest {
        val id = addEurPln()

        repository.delete(id)
        repository.forgetDeleted()

        assertNull(repository.observeDeleted().first())
        assertNull(repository.byId(id))
    }

    @Test
    fun thereIsNothingToUndoWhenNothingWasDeleted() = runTest {
        assertEquals(false, repository.undoDelete())
    }

    @Test
    fun undoingTwiceDoesNotDuplicateThePair() = runTest {
        val id = addEurPln()

        repository.delete(id)
        repository.undoDelete()
        repository.undoDelete()

        assertEquals(1, watchedPairsDao.getAll().size)
    }

    @Test
    fun deletingAPairThatIsNotThereChangesNothing() = runTest {
        addEurPln()

        assertNull(repository.delete(404))
        assertEquals(1, watchedPairsDao.getAll().size)
    }

    private suspend fun addEurPln(): Long {
        val id = repository.add(pair("EUR", "PLN"), ProviderKey("NBP")).id
        addRate()
        return id
    }

    private suspend fun addRate(
        base: String = "EUR",
        quote: String = "PLN",
        provider: String = "NBP",
        date: String = "2026-11-17",
    ) {
        ratesDao.upsertAll(
            listOf(
                RateEntity(
                    base = base,
                    quote = quote,
                    date = date,
                    sourceId = SourceIdColumn.Frankfurter,
                    provider = provider,
                    value = "4.4032",
                    fetchedAt = clock.now().toEpochMilliseconds(),
                )
            )
        )
    }

    private fun pair(base: String, quote: String) =
        CurrencyPair(CurrencyCode(base), CurrencyCode(quote))
}
