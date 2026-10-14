package dev.faizksen.kursor.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.faizksen.kursor.domain.SyncScheduler
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncSchedulerImpl @Inject constructor(
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
            periodicRequest(),
        )
    }

    private fun periodicRequest() = PeriodicWorkRequestBuilder<FiatRatesSyncWorker>(
        repeatInterval = 6, repeatIntervalTimeUnit = TimeUnit.HOURS,
        flexTimeInterval = 1, flexTimeIntervalUnit = TimeUnit.HOURS
    ).setInputData(
        triggerData(SyncTrigger.Periodic)
    ).setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .build()
    ).setBackoffCriteria(
        backoffPolicy = BackoffPolicy.EXPONENTIAL,
        backoffDelay = 30,
        timeUnit = TimeUnit.SECONDS
    ).build()

    override fun requestImmediateSync() {
        workManager.enqueueUniqueWork(
            uniqueWorkName = FIAT_IMMEDIATE_SYNC_NAME,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = OneTimeWorkRequestBuilder<FiatRatesSyncWorker>()
                .setInputData(triggerData(SyncTrigger.Manual))
                .build()
        )
    }

    private fun triggerData(trigger: SyncTrigger) =
        workDataOf(FiatRatesSyncWorker.KEY_TRIGGER to trigger.name)

    override fun observeSyncQueue(trigger: SyncTrigger): Flow<SyncQueue> =
        workManager.getWorkInfosForUniqueWorkFlow(trigger.uniqueWorkName())
            .map { infos -> infos.firstOrNull().toSyncQueue() }
            .distinctUntilChanged()

    private fun SyncTrigger.uniqueWorkName(): String = when (this) {
        SyncTrigger.Periodic -> FIAT_PERIODIC_SYNC_NAME
        SyncTrigger.Manual -> FIAT_IMMEDIATE_SYNC_NAME
    }

    private fun WorkInfo?.toSyncQueue(): SyncQueue = if (this == null) {
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
        ?.let(Instant::ofEpochMilli)

    private fun WorkInfo?.toSyncState(): SyncState = when (this?.state) {
        WorkInfo.State.RUNNING -> SyncState.Running
        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> SyncState.Scheduled
        WorkInfo.State.SUCCEEDED -> SyncState.Succeeded
        WorkInfo.State.FAILED -> SyncState.Failed
        else -> SyncState.Idle
    }
}
