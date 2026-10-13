package dev.faizksen.kursor.data

import dev.faizksen.kursor.data.mappers.toSyncLogEntity
import dev.faizksen.kursor.data.mappers.toSyncRun
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.model.SyncRun
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

interface SyncLogRepository {
    suspend fun record(run: SyncRun): Long
    fun observeRecent(limit: Int): Flow<List<SyncRun>>
    fun observeLastSuccess(): Flow<SyncRun?>
    suspend fun deleteOlderThan(threshold: Instant)
}

class SyncLogRepositoryImpl @Inject constructor(
    private val syncLogDao: SyncLogDao,
): SyncLogRepository {

    override suspend fun record(run: SyncRun): Long = syncLogDao.insert(run.toSyncLogEntity())

    override fun observeRecent(limit: Int): Flow<List<SyncRun>> =
        syncLogDao.observeRecent(limit).map { entities -> entities.mapNotNull { it.toSyncRun() } }

    override fun observeLastSuccess(): Flow<SyncRun?> =
        syncLogDao.observeLastSuccess().map { entity -> entity?.toSyncRun() }

    override suspend fun deleteOlderThan(threshold: Instant) =
        syncLogDao.deleteOlderThan(threshold.toEpochMilli())
}
