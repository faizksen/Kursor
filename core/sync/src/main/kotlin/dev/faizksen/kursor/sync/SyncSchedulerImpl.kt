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
import dev.faizksen.kursor.domain.SyncScheduler
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                ).build()
        )
    }

    override fun observeSyncState(trigger: SyncTrigger): Flow<SyncState> =
        workManager.getWorkInfosForUniqueWorkFlow(trigger.uniqueWorkName())
            .map { infos -> infos.firstOrNull().toSyncState() }
            .distinctUntilChanged()

    private fun SyncTrigger.uniqueWorkName(): String = when (this) {
        SyncTrigger.Periodic -> FIAT_PERIODIC_SYNC_NAME
        SyncTrigger.Manual -> FIAT_IMMEDIATE_SYNC_NAME
    }

    private fun WorkInfo?.toSyncState(): SyncState = when (this?.state) {
        WorkInfo.State.RUNNING -> SyncState.Running
        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> SyncState.Scheduled
        WorkInfo.State.SUCCEEDED -> SyncState.Succeeded
        WorkInfo.State.FAILED -> SyncState.Failed
        else -> SyncState.Idle
    }
}
