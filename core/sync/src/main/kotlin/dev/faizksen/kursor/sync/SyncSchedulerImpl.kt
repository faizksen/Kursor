package dev.faizksen.kursor.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.faizksen.kursor.domain.SyncScheduler
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.time.Instant
import java.util.concurrent.TimeUnit

class SyncSchedulerImpl(
    private val workManager: WorkManager,
) : SyncScheduler {

    companion object {
        private const val FIAT_PERIODIC_SYNC_NAME = "fiat_rates_periodic_sync"
        private const val FIAT_IMMEDIATE_SYNC_NAME = "fiat_rates_immediate_sync"
    }

    override fun schedulePeriodicSync() {
        workManager.enqueueUniquePeriodicWork(
            FIAT_PERIODIC_SYNC_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicSyncRequest(),
        )
    }

    override fun requestImmediateSync() {
        workManager.enqueueUniqueWork(
            uniqueWorkName = FIAT_IMMEDIATE_SYNC_NAME,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = immediateSyncRequest(),
        )
    }

    override fun requestPairSync(pair: CurrencyPair) {
        workManager.enqueueUniqueWork(
            uniqueWorkName = pair.uniqueWorkName(),
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = pairSyncRequest(pair),
        )
    }

    override fun observeSyncQueue(trigger: SyncTrigger): Flow<SyncQueue> =
        workManager.getWorkInfosForUniqueWorkFlow(trigger.uniqueWorkName())
            .map { infos -> infos.firstOrNull().toSyncQueue() }
            .distinctUntilChanged()

    private fun SyncTrigger.uniqueWorkName(): String = when (this) {
        SyncTrigger.Periodic -> FIAT_PERIODIC_SYNC_NAME
        SyncTrigger.Manual, SyncTrigger.PairAdded -> FIAT_IMMEDIATE_SYNC_NAME
    }
}

internal fun periodicSyncRequest(): PeriodicWorkRequest =
    PeriodicWorkRequestBuilder<FiatRatesSyncWorker>(
        repeatInterval = SYNC_INTERVAL_HOURS, repeatIntervalTimeUnit = TimeUnit.HOURS,
        flexTimeInterval = SYNC_FLEX_HOURS, flexTimeIntervalUnit = TimeUnit.HOURS,
    ).setInputData(
        triggerData(SyncTrigger.Periodic)
    ).setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .build()
    ).setBackoffCriteria(
        backoffPolicy = BackoffPolicy.EXPONENTIAL,
        backoffDelay = SYNC_BACKOFF_SECONDS,
        timeUnit = TimeUnit.SECONDS,
    ).build()

internal fun immediateSyncRequest(): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<FiatRatesSyncWorker>()
        .setInputData(triggerData(SyncTrigger.Manual))
        .build()

internal fun pairSyncRequest(pair: CurrencyPair): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<FiatRatesSyncWorker>()
        .setInputData(
            workDataOf(
                FiatRatesSyncWorker.KEY_TRIGGER to SyncTrigger.PairAdded.name,
                FiatRatesSyncWorker.KEY_BASE to pair.base.code,
                FiatRatesSyncWorker.KEY_QUOTE to pair.quote.code,
            )
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

internal fun CurrencyPair.uniqueWorkName(): String =
    FIAT_PAIR_SYNC_PREFIX + base.code + "_" + quote.code

internal const val FIAT_PAIR_SYNC_PREFIX = "fiat_rates_pair_sync_"

private fun triggerData(trigger: SyncTrigger) =
    workDataOf(FiatRatesSyncWorker.KEY_TRIGGER to trigger.name)

internal const val SYNC_INTERVAL_HOURS = 6L
internal const val SYNC_FLEX_HOURS = 1L
internal const val SYNC_BACKOFF_SECONDS = 30L

internal fun WorkInfo?.toSyncQueue(): SyncQueue = if (this == null) {
    SyncQueue.Idle
} else {
    SyncQueue(
        state = toSyncState(),
        nextRunAt = nextRunAt(),
        attemptCount = runAttemptCount,
    )
}

private fun WorkInfo.nextRunAt(): Instant? = nextScheduleTimeMillis
    .takeIf { state == WorkInfo.State.ENQUEUED && it != Long.MAX_VALUE }
    ?.let { Instant.fromEpochMilliseconds(it) }

private fun WorkInfo?.toSyncState(): SyncState = when (this?.state) {
    WorkInfo.State.RUNNING -> SyncState.Running
    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> SyncState.Scheduled
    WorkInfo.State.SUCCEEDED -> SyncState.Succeeded
    WorkInfo.State.FAILED -> SyncState.Failed
    else -> SyncState.Idle
}
