package dev.faizksen.kursor.domain


fun interface RequestManualSyncUseCase {
    operator fun invoke()
}

internal class RequestManualSyncUseCaseImpl(
    private val syncScheduler: SyncScheduler,
): RequestManualSyncUseCase {
    override operator fun invoke() = syncScheduler.requestImmediateSync()
}
