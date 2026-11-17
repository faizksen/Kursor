package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.SyncLogRepository
import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.SyncRun
import kotlinx.coroutines.flow.Flow

fun interface ObserveRunUseCase {
    operator fun invoke(runId: Long): Flow<SyncRun?>
}

fun interface ObserveRunAttemptsUseCase {
    operator fun invoke(runId: Long): Flow<List<ProviderAttempt>>
}

internal class ObserveRunUseCaseImpl(
    private val syncLogRepository: SyncLogRepository,
): ObserveRunUseCase {
    override fun invoke(runId: Long): Flow<SyncRun?> = syncLogRepository.observeRun(runId)
}

internal class ObserveRunAttemptsUseCaseImpl(
    private val syncLogRepository: SyncLogRepository,
): ObserveRunAttemptsUseCase {
    override fun invoke(runId: Long): Flow<List<ProviderAttempt>> =
        syncLogRepository.observeAttempts(runId)
}
