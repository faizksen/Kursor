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

    @Test
    fun latestRateIsTheOneFetchedLastEvenWhenAnotherCarriesALaterDate() = runTest {
        dao.upsertAll(
            listOf(
                rate(date = "2026-11-08", value = "4.4016", fetchedAt = 1_000L),
                rate(date = "2026-11-06", value = "4.3900", fetchedAt = 2_000L),
            )
        )

        val stored = dao.observeLatestRates().first()

        assertEquals(1, stored.size)
        assertEquals("4.3900", stored[0].value)
        assertEquals("2026-11-06", stored[0].date)
    }

    @Test
    fun eachSourceKeepsItsOwnLatestRate() = runTest {
        dao.upsertAll(
            listOf(
                rate(date = "2026-11-06", value = "4.3900", fetchedAt = 2_000L),
                rate(
                    date = "2026-11-05",
                    value = "4.4100",
                    fetchedAt = 1_000L,
                    sourceId = SourceIdColumn.CBR,
                ),
            )
        )

        val stored = dao.observeLatestRates().first()

        assertEquals(2, stored.size)
        assertEquals(
            setOf(SourceIdColumn.Frankfurter, SourceIdColumn.CBR),
            stored.map { it.sourceId }.toSet(),
        )
    }

    private fun rate(
        date: String,
        value: String,
        fetchedAt: Long,
        sourceId: SourceIdColumn = SourceIdColumn.Frankfurter,
    ) = RateEntity(
        base = "EUR",
        quote = "PLN",
        date = date,
        sourceId = sourceId,
        value = value,
        fetchedAt = fetchedAt,
    )
}
