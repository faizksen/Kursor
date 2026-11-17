package dev.faizksen.kursor.data

import dev.faizksen.kursor.data.mappers.toEntity
import dev.faizksen.kursor.data.mappers.toProviderAttempt
import dev.faizksen.kursor.data.mappers.toSyncLogEntity
import dev.faizksen.kursor.data.mappers.toSyncRun
import dev.faizksen.kursor.database.dao.SyncAttemptDao
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.SyncRun
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

interface SyncLogRepository {
    suspend fun record(run: SyncRun, attempts: List<ProviderAttempt> = emptyList()): Long
    fun observeAttempts(runId: Long): Flow<List<ProviderAttempt>>
    fun observeRecent(limit: Int): Flow<List<SyncRun>>
    fun observeLastSuccess(): Flow<SyncRun?>
    suspend fun deleteOlderThan(threshold: Instant)
}

internal class SyncLogRepositoryImpl(
    private val syncLogDao: SyncLogDao,
    private val syncAttemptDao: SyncAttemptDao,
): SyncLogRepository {

    override suspend fun record(run: SyncRun, attempts: List<ProviderAttempt>): Long {
        val runId = syncLogDao.insert(run.toSyncLogEntity())

        if (attempts.isNotEmpty()) {
            syncAttemptDao.insertAll(attempts.map { attempt -> attempt.toEntity(runId) })
        }

        return runId
    }

    override fun observeAttempts(runId: Long): Flow<List<ProviderAttempt>> = syncAttemptDao
        .observeForRun(runId)
        .map { entities -> entities.map { entity -> entity.toProviderAttempt() } }

    override fun observeRecent(limit: Int): Flow<List<SyncRun>> =
        syncLogDao.observeRecent(limit).map { entities -> entities.mapNotNull { it.toSyncRun() } }

    override fun observeLastSuccess(): Flow<SyncRun?> =
        syncLogDao.observeLastSuccess().map { entity -> entity?.toSyncRun() }

    override suspend fun deleteOlderThan(threshold: Instant) =
        syncLogDao.deleteOlderThan(threshold.toEpochMilliseconds())
}
