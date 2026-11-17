package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.SyncLogRepository
import kotlinx.coroutines.flow.first

fun interface SyncOnFirstLaunchUseCase {
    suspend operator fun invoke()
}

internal class SyncOnFirstLaunchUseCaseImpl(
    private val syncLogRepository: SyncLogRepository,
    private val syncScheduler: SyncScheduler,
): SyncOnFirstLaunchUseCase {

    override suspend fun invoke() {
        if (syncLogRepository.observeLastSuccess().first() == null) {
            syncScheduler.requestImmediateSync()
        }
    }
}
