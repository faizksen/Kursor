package dev.faizksen.kursor.database

import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.entities.RateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RatesDaoTest {

    private lateinit var database: KursorDatabase
    private lateinit var dao: RatesDao

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        dao = database.ratesDao
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertOfSameRateReplacesValueInsteadOfDuplicating() = runTest {
        val rate = RateEntity(
            base = "EUR",
            quote = "PLN",
            date = "2026-09-06",
            sourceId = SourceIdColumn.Frankfurter,
            value = "4.3244",
            fetchedAt = 1_000L,
        )

        dao.upsertAll(listOf(rate))
        dao.upsertAll(listOf(rate.copy(value = "4.5000", fetchedAt = 2_000L)))

        val stored = dao.observeLatestRates().first()

        assertEquals(1, stored.size)
        assertEquals("4.5000", stored[0].value)
        assertEquals(2_000L, stored[0].fetchedAt)
    }
}
