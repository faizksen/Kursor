package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.SyncLogRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

fun interface PruneSyncLogUseCase {
    suspend operator fun invoke()
}

internal class PruneSyncLogUseCaseImpl(
    private val syncLogRepository: SyncLogRepository,
    private val clock: Clock,
): PruneSyncLogUseCase {

    override suspend fun invoke() =
        syncLogRepository.deleteOlderThan(clock.now() - SYNC_LOG_RETENTION)
}

internal val SYNC_LOG_RETENTION = 30.days
