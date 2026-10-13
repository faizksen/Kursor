package dev.faizksen.kursor.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.faizksen.kursor.domain.RefreshRatesUseCase
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.SyncTrigger

@HiltWorker
class FiatRatesSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val refreshRates: RefreshRatesUseCase,
    private val syncLogger: SyncLogger,
): CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_TRIGGER = "sync_trigger"
        private val FALLBACK_TRIGGER = SyncTrigger.Periodic
    }

    override suspend fun doWork(): Result = syncLogger.log(
        sourceId = SourceId.FRANKFURTER,
        trigger = inputData.syncTrigger(),
        attempt = runAttemptCount + 1,
    ) {
        refreshRates()
    }.toWorkerResult()

    private fun Data.syncTrigger(): SyncTrigger = getString(KEY_TRIGGER)
        ?.let { name -> SyncTrigger.entries.firstOrNull { it.name == name } }
        ?: FALLBACK_TRIGGER

    private fun SyncOutcome.toWorkerResult(): Result = when (this) {
        is SyncOutcome.Success -> Result.success()
        is SyncOutcome.Transient -> Result.retry()
        is SyncOutcome.Permanent -> Result.failure()
    }
}
