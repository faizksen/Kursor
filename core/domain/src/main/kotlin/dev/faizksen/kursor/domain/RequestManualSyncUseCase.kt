package dev.faizksen.kursor.domain

import javax.inject.Inject

class RequestManualSyncUseCase @Inject constructor(
    private val syncScheduler: SyncScheduler,
) {
    operator fun invoke() = syncScheduler.requestImmediateSync()
}
