package dev.faizksen.kursor.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dev.faizksen.kursor.domain.RefreshCatalogueUseCase
import dev.faizksen.kursor.domain.RunSyncUseCase
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.CancellationException

class FiatRatesSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val runSync: RunSyncUseCase,
    private val refreshCatalogue: RefreshCatalogueUseCase,
): CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_TRIGGER = "sync_trigger"
        const val KEY_BASE = "sync_base"
        const val KEY_QUOTE = "sync_quote"
    }

    override suspend fun doWork(): Result {
        val attempt = runAttemptCount + 1
        val canRetry = attempt < MAX_SYNC_ATTEMPTS
        val trigger = inputData.syncTrigger()

        if (trigger == SyncTrigger.Periodic) {
            refreshCatalogueIgnoringFailure()
        }

        return runSync(
            sourceId = SourceId.FRANKFURTER,
            trigger = trigger,
            attempt = attempt,
            canRetry = canRetry,
            only = inputData.syncPair(),
        ).toWorkerResult(canRetry)
    }

    private suspend fun refreshCatalogueIgnoringFailure() {
        try {
            refreshCatalogue()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (ignored: Throwable) {
        }
    }
}

internal const val MAX_SYNC_ATTEMPTS = 3

private val FALLBACK_TRIGGER = SyncTrigger.Periodic

internal fun Data.syncTrigger(): SyncTrigger = getString(FiatRatesSyncWorker.KEY_TRIGGER)
    ?.let { name -> SyncTrigger.entries.firstOrNull { it.name == name } }
    ?: FALLBACK_TRIGGER

internal fun Data.syncPair(): CurrencyPair? {
    val base = getString(FiatRatesSyncWorker.KEY_BASE)?.takeIf { it.isNotBlank() } ?: return null
    val quote = getString(FiatRatesSyncWorker.KEY_QUOTE)?.takeIf { it.isNotBlank() } ?: return null

    return CurrencyPair(base = CurrencyCode(base), quote = CurrencyCode(quote))
}

internal fun SyncOutcome.toWorkerResult(canRetry: Boolean): ListenableWorker.Result = when (this) {
    is SyncOutcome.Success -> ListenableWorker.Result.success()
    is SyncOutcome.Transient -> if (canRetry) {
        ListenableWorker.Result.retry()
    } else {
        ListenableWorker.Result.failure()
    }
    is SyncOutcome.Permanent -> ListenableWorker.Result.failure()
}
