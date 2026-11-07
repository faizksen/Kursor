package dev.faizksen.kursor.database

import dev.faizksen.kursor.database.dao.WatchedPairsDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WatchedPairsDaoTest {

    private lateinit var database: KursorDatabase
    private lateinit var watchedPairsDao: WatchedPairsDao

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        watchedPairsDao = database.watchedPairsDao
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAtEndAssignsIncrementingPositions() = runTest {
        watchedPairsDao.insertAtEnd(base = "EUR", quote = "USD", addedAt = 1_000L)
        watchedPairsDao.insertAtEnd(base = "USD", quote = "PLN", addedAt = 2_000L)

        val results = watchedPairsDao.observeAll().first()
        assertEquals(2, results.size)

        val firstPair = results[0]
        assertEquals("EUR", firstPair.base)
        assertEquals("USD", firstPair.quote)
        assertEquals(1_000L, firstPair.addedAt)
        assertEquals(0, firstPair.position)

        val secondPair = results[1]
        assertEquals("USD", secondPair.base)
        assertEquals("PLN", secondPair.quote)
        assertEquals(2_000L, secondPair.addedAt)
        assertEquals(1, secondPair.position)
    }

    @Test
    fun addAtEndIgnoresDuplicateAndReportsExistingPair() = runTest {
        val first = watchedPairsDao.addAtEnd(base = "EUR", quote = "USD", addedAt = 1_000L)
        val second = watchedPairsDao.addAtEnd(base = "EUR", quote = "USD", addedAt = 2_000L)

        assertFalse(first.alreadyExisted)
        assertTrue(second.alreadyExisted)
        assertEquals(first.id, second.id)

        val results = watchedPairsDao.observeAll().first()
        assertEquals(1, results.size)
        assertEquals(1_000L, results[0].addedAt)

        val resultsNotOrdered = watchedPairsDao.getAll()
        assertEquals(1, resultsNotOrdered.size)
        assertEquals(1_000L, resultsNotOrdered[0].addedAt)
    }

    @Test
    fun getAllReturnsSamePairsAsObserveAll() = runTest {
        watchedPairsDao.addAtEnd(base = "EUR", quote = "USD", addedAt = 1_000L)
        watchedPairsDao.addAtEnd(base = "EUR", quote = "PLN", addedAt = 2_000L)

        val results = watchedPairsDao.observeAll().first()
        val resultsNotOrdered = watchedPairsDao.getAll()

        assertEquals(results.toSet(), resultsNotOrdered.toSet())
    }

    @Test
    fun getAllReturnsActualPairs() = runTest {
        watchedPairsDao.addAtEnd(base = "EUR", quote = "USD", addedAt = 1_000L)
        watchedPairsDao.addAtEnd(base = "EUR", quote = "PLN", addedAt = 2_000L)

        val results = watchedPairsDao.getAll()

        assertEquals(
            setOf("EUR" to "USD", "EUR" to "PLN"),
            results.map { it.base to it.quote }.toSet(),
        )
    }
}
