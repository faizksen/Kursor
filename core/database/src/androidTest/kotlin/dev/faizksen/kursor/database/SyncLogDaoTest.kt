package dev.faizksen.kursor.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.database.consts.ErrorColumn
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.consts.SyncResultColumn
import dev.faizksen.kursor.database.consts.TriggerColumn
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.database.entities.SyncLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncLogDaoTest {

    private lateinit var database: KursorDatabase
    private lateinit var dao: SyncLogDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KursorDatabase::class.java,
        ).build()
        dao = database.syncLogDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeLastSuccessEmptyTable() = runTest {
        val stored = dao.observeLastSuccess().first()
        assertThat(stored).isNull()
    }

    @Test
    fun observeRecentEmptyTable() = runTest {
        val stored = dao.observeRecent(1).first()
        assertThat(stored).hasSize(0)
    }

    @Test
    fun insertTest() = runTest {
        val entity = entry(
            finishedAt = 1000,
            result = SyncResultColumn.Failed,
            startedAt = 50,
            trigger = TriggerColumn.Periodic,
            sourceId = SourceIdColumn.Frankfurter,
            errorKind = ErrorColumn.Timeout,
            updatedCount = 0,
            attempt = 1
        )
        dao.insert(entity)
        val stored = dao.observeRecent(1).firstOrNull()?.firstOrNull()
        assertThat(stored).isNotNull()
        assertThat(stored!!.result).isEqualTo(entity.result)
        assertThat(stored.trigger).isEqualTo(entity.trigger)
        assertThat(stored.sourceId).isEqualTo(entity.sourceId)
        assertThat(stored.errorKind).isEqualTo(entity.errorKind)
    }

    @Test
    fun syncLogEntityNullableFields() = runTest {
        val entity = entry(
            finishedAt = 100,
            errorKind = null,
            updatedCount = null
        )
        dao.insert(entity)
        val stored = dao.observeRecent(1).first().firstOrNull()
        assertThat(stored).isNotNull()
        assertThat(stored!!.errorKind).isNull()
        assertThat(stored.updatedCount).isNull()
    }

    @Test
    fun observeRecentOrder() = runTest {
        val entity1 = entry(finishedAt = 1000)
        val entity2 = entry(finishedAt = 500)
        val entity3 = entry(finishedAt = 1500)
        dao.insert(entity1)
        dao.insert(entity2)
        dao.insert(entity3)
        val stored = dao.observeRecent(2).first()
        assertThat(stored).hasSize(2)
        assertThat(stored[0].finishedAt).isEqualTo(1500)
        assertThat(stored[1].finishedAt).isEqualTo(1000)
    }

    @Test
    fun observeLastSuccess() = runTest {
        val entitySucceed = entry(finishedAt = 1000, result = SyncResultColumn.Success)
        val entityFailed = entry(finishedAt = 1500, result = SyncResultColumn.Failed)
        dao.insert(entitySucceed)
        dao.insert(entityFailed)
        val stored = dao.observeLastSuccess().first()
        assertThat(stored).isNotNull()
        assertThat(stored!!.result).isEqualTo(SyncResultColumn.Success)
        assertThat(stored.finishedAt).isEqualTo(1000)
    }

    @Test
    fun observeLastSuccessOrder() = runTest {
        val entitySucceed = entry(finishedAt = 1000, result = SyncResultColumn.Success)
        val entitySucceedLatest = entry(finishedAt = 1500, result = SyncResultColumn.Success)
        val entityRetried = entry(finishedAt = 2000, result = SyncResultColumn.Retry)
        dao.insert(entitySucceedLatest)
        dao.insert(entitySucceed)
        dao.insert(entityRetried)
        val stored = dao.observeLastSuccess().first()
        assertThat(stored).isNotNull()
        assertThat(stored!!.result).isEqualTo(SyncResultColumn.Success)
        assertThat(stored.finishedAt).isEqualTo(1500)
    }

    @Test
    fun deleteOlderThan() = runTest {
        val entity1 = entry(finishedAt = 100)
        val entity2 = entry(finishedAt = 101)
        val entity3 = entry(finishedAt = 99)
        dao.insert(entity1)
        dao.insert(entity2)
        dao.insert(entity3)
        dao.deleteOlderThan(100)
        val stored = dao.observeRecent(3).first()
        assertThat(stored.map { it.finishedAt }).containsExactly(101L, 100L)
    }

    private fun entry(
        finishedAt: Long,
        result: SyncResultColumn = SyncResultColumn.Success,
        startedAt: Long = finishedAt - 100,
        trigger: TriggerColumn = TriggerColumn.Periodic,
        sourceId: SourceIdColumn = SourceIdColumn.Frankfurter,
        errorKind: ErrorColumn? = null,
        updatedCount: Int? = 0,
        attempt: Int = 1,
    ) = SyncLogEntity(
        id = 0,
        sourceId = sourceId,
        trigger = trigger,
        startedAt = startedAt,
        finishedAt = finishedAt,
        result = result,
        errorKind = errorKind,
        updatedCount = updatedCount,
        attempt = attempt
    )
}
