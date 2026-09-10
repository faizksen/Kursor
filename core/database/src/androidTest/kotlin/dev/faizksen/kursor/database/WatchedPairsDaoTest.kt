package dev.faizksen.kursor.database

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.WatchedPairEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchedPairsDaoTest {
    private lateinit var database: KursorDatabase
    private lateinit var watchedPairsDao: WatchedPairsDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            context = ApplicationProvider.getApplicationContext(),
            klass = KursorDatabase::class.java
        ).build()
        watchedPairsDao = database.watchedPairsDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAtEndAssignsIncrementingPositions() = runTest {
        watchedPairsDao.insertAtEnd(base = "EUR", quote = "USD", addedAt = 1_000L)
        watchedPairsDao.insertAtEnd(base = "USD", quote = "PLN", addedAt = 2_000L)
        val results = watchedPairsDao.observeAll().first()
        assertThat(results).hasSize(2)

        val firstPair = results[0]
        assertThat(firstPair.base).isEqualTo("EUR")
        assertThat(firstPair.quote).isEqualTo("USD")
        assertThat(firstPair.addedAt).isEqualTo(1_000L)
        assertThat(firstPair.position).isEqualTo(0)

        val secondPair = results[1]
        assertThat(secondPair.base).isEqualTo("USD")
        assertThat(secondPair.quote).isEqualTo("PLN")
        assertThat(secondPair.addedAt).isEqualTo(2_000L)
        assertThat(secondPair.position).isEqualTo(1)
    }

    @Test
    fun addAtEndIgnoresDuplicateAndReportsExistingPair() = runTest {
        val first = watchedPairsDao.addAtEnd(base = "EUR", quote = "USD", addedAt = 1_000L)
        val second = watchedPairsDao.addAtEnd(base = "EUR", quote = "USD", addedAt = 2_000L)

        assertThat(first.alreadyExisted).isFalse()
        assertThat(second.alreadyExisted).isTrue()
        assertThat(second.id).isEqualTo(first.id)

        val results = watchedPairsDao.observeAll().first()
        assertThat(results).hasSize(1)
        assertThat(results[0].addedAt).isEqualTo(1_000L)

        val resultsUNotOrdered = watchedPairsDao.getAll()
        assertThat(resultsUNotOrdered).hasSize(1)
        assertThat(resultsUNotOrdered[0].addedAt).isEqualTo(1_000L)
    }

    @Test
    fun getAllReturnsSamePairsAsObserveAll() = runTest {
        watchedPairsDao.addAtEnd(base = "EUR", quote = "USD", addedAt = 1_000L)
        watchedPairsDao.addAtEnd(base = "EUR", quote = "PLN", addedAt = 2_000L)

        val results = watchedPairsDao.observeAll().first()
        val resultsNotOrdered = watchedPairsDao.getAll()

        assertThat(resultsNotOrdered).containsExactlyElementsIn(results)
    }

    @Test
    fun getAllReturnsActualPairs() = runTest {
        watchedPairsDao.addAtEnd(base = "EUR", quote = "USD", addedAt = 1_000L)
        watchedPairsDao.addAtEnd(base = "EUR", quote = "PLN", addedAt = 2_000L)

        val results = watchedPairsDao.getAll()

        assertThat(results.map { it.base to it.quote })
            .containsExactly("EUR" to "USD", "EUR" to "PLN")
    }
}