package dev.faizksen.kursor.database

import dev.faizksen.kursor.database.consts.ErrorColumn
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.consts.SyncResultColumn
import dev.faizksen.kursor.database.consts.TriggerColumn
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.database.entities.SyncLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SyncLogDaoTest {

    private lateinit var database: KursorDatabase
    private lateinit var dao: SyncLogDao

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        dao = database.syncLogDao
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeLastSuccessEmptyTable() = runTest {
        assertNull(dao.observeLastSuccess().first())
    }

    @Test
    fun observeRecentEmptyTable() = runTest {
        assertEquals(0, dao.observeRecent(1).first().size)
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
            attempt = 1,
        )

        dao.insert(entity)

        val stored = assertNotNull(dao.observeRecent(1).firstOrNull()?.firstOrNull())
        assertEquals(entity.result, stored.result)
        assertEquals(entity.trigger, stored.trigger)
        assertEquals(entity.sourceId, stored.sourceId)
        assertEquals(entity.errorKind, stored.errorKind)
    }

    @Test
    fun syncLogEntityNullableFields() = runTest {
        dao.insert(entry(finishedAt = 100, errorKind = null, updatedCount = null))

        val stored = assertNotNull(dao.observeRecent(1).first().firstOrNull())
        assertNull(stored.errorKind)
        assertNull(stored.updatedCount)
    }

    @Test
    fun observeRecentOrder() = runTest {
        dao.insert(entry(finishedAt = 1000))
        dao.insert(entry(finishedAt = 500))
        dao.insert(entry(finishedAt = 1500))

        val stored = dao.observeRecent(2).first()

        assertEquals(2, stored.size)
        assertEquals(1500, stored[0].finishedAt)
        assertEquals(1000, stored[1].finishedAt)
    }

    @Test
    fun observeLastSuccess() = runTest {
        dao.insert(entry(finishedAt = 1000, result = SyncResultColumn.Success))
        dao.insert(entry(finishedAt = 1500, result = SyncResultColumn.Failed))

        val stored = assertNotNull(dao.observeLastSuccess().first())
        assertEquals(SyncResultColumn.Success, stored.result)
        assertEquals(1000, stored.finishedAt)
    }

    @Test
    fun observeLastSuccessOrder() = runTest {
        dao.insert(entry(finishedAt = 1500, result = SyncResultColumn.Success))
        dao.insert(entry(finishedAt = 1000, result = SyncResultColumn.Success))
        dao.insert(entry(finishedAt = 2000, result = SyncResultColumn.Retry))

        val stored = assertNotNull(dao.observeLastSuccess().first())
        assertEquals(SyncResultColumn.Success, stored.result)
        assertEquals(1500, stored.finishedAt)
    }

    @Test
    fun deleteOlderThan() = runTest {
        dao.insert(entry(finishedAt = 100))
        dao.insert(entry(finishedAt = 101))
        dao.insert(entry(finishedAt = 99))

        dao.deleteOlderThan(100)

        val stored = dao.observeRecent(3).first()
        assertEquals(listOf(101L, 100L), stored.map { it.finishedAt })
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
        attempt = attempt,
    )
}
