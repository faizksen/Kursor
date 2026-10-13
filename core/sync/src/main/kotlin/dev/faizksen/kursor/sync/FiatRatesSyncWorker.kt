package dev.faizksen.kursor.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.faizksen.kursor.domain.RefreshRatesUseCase
import dev.faizksen.kursor.model.SyncOutcome

@HiltWorker
class FiatRatesSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val refreshRates: RefreshRatesUseCase
): CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = when(refreshRates()) {
        is SyncOutcome.Permanent -> Result.failure()
        is SyncOutcome.Success -> Result.success()
        is SyncOutcome.Transient -> Result.retry()
    }
}