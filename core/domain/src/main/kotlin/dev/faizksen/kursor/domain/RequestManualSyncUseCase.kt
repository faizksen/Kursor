package dev.faizksen.kursor.domain

import javax.inject.Inject

fun interface RequestManualSyncUseCase {
    operator fun invoke()
}

internal class RequestManualSyncUseCaseImpl @Inject constructor(
    private val syncScheduler: SyncScheduler,
): RequestManualSyncUseCase {
    override operator fun invoke() = syncScheduler.requestImmediateSync()
}
