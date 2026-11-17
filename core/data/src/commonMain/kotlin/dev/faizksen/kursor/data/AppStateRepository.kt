package dev.faizksen.kursor.data

import dev.faizksen.kursor.database.dao.AppStateDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Clock

interface AppStateRepository {
    suspend fun defaultsSeeded(): Boolean
    fun observeDefaultsSeeded(): Flow<Boolean>
    suspend fun markDefaultsSeeded()
}

internal class AppStateRepositoryImpl(
    private val appStateDao: AppStateDao,
    private val clock: Clock,
): AppStateRepository {

    override suspend fun defaultsSeeded(): Boolean = appStateDao.defaultsSeeded()

    override fun observeDefaultsSeeded(): Flow<Boolean> =
        appStateDao.observeDefaultsSeeded().distinctUntilChanged()

    override suspend fun markDefaultsSeeded() =
        appStateDao.markDefaultsSeeded(clock.now().toEpochMilliseconds())
}
