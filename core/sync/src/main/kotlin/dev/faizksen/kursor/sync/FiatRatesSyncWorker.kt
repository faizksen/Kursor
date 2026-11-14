package dev.faizksen.kursor.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dev.faizksen.kursor.domain.RunSyncUseCase
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.SyncTrigger

class FiatRatesSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val runSync: RunSyncUseCase,
): CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_TRIGGER = "sync_trigger"
    }

    override suspend fun doWork(): Result {
        val attempt = runAttemptCount + 1
        val canRetry = attempt < MAX_SYNC_ATTEMPTS

        return runSync(
            sourceId = SourceId.FRANKFURTER,
            trigger = inputData.syncTrigger(),
            attempt = attempt,
            canRetry = canRetry,
        ).toWorkerResult(canRetry)
    }
}

internal const val MAX_SYNC_ATTEMPTS = 3

private val FALLBACK_TRIGGER = SyncTrigger.Periodic

internal fun Data.syncTrigger(): SyncTrigger = getString(FiatRatesSyncWorker.KEY_TRIGGER)
    ?.let { name -> SyncTrigger.entries.firstOrNull { it.name == name } }
    ?: FALLBACK_TRIGGER

internal fun SyncOutcome.toWorkerResult(canRetry: Boolean): ListenableWorker.Result = when (this) {
    is SyncOutcome.Success -> ListenableWorker.Result.success()
    is SyncOutcome.Transient -> if (canRetry) {
        ListenableWorker.Result.retry()
    } else {
        ListenableWorker.Result.failure()
    }
    is SyncOutcome.Permanent -> ListenableWorker.Result.failure()
}
