package dev.faizksen.kursor.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.entities.RateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RatesDaoTest {

    private lateinit var database: KursorDatabase
    private lateinit var dao: RatesDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KursorDatabase::class.java,
        ).build()
        dao = database.ratesDao
    }

    @After
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

        assertThat(stored).hasSize(1)
        assertThat(stored[0].value).isEqualTo("4.5000")
        assertThat(stored[0].fetchedAt).isEqualTo(2_000L)
    }
}
