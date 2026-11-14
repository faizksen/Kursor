package dev.faizksen.kursor.sync

import androidx.work.BackoffPolicy
import androidx.work.NetworkType
import dev.faizksen.kursor.model.SyncTrigger
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class SyncRequestTest {

    @Test
    fun `the periodic sync runs every six hours with an hour of slack`() {
        val spec = periodicSyncRequest().workSpec

        assertEquals(TimeUnit.HOURS.toMillis(6), spec.intervalDuration)
        assertEquals(TimeUnit.HOURS.toMillis(1), spec.flexDuration)
    }

    @Test
    fun `the periodic sync stays off roaming networks`() {
        assertEquals(
            NetworkType.NOT_ROAMING,
            periodicSyncRequest().workSpec.constraints.requiredNetworkType,
        )
    }

    @Test
    fun `the periodic sync backs off exponentially from thirty seconds`() {
        val spec = periodicSyncRequest().workSpec

        assertEquals(BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(TimeUnit.SECONDS.toMillis(30), spec.backoffDelayDuration)
    }

    @Test
    fun `the periodic sync tells the worker it was not asked for`() {
        assertEquals(
            SyncTrigger.Periodic,
            periodicSyncRequest().workSpec.input.syncTrigger(),
        )
    }

    @Test
    fun `an immediate sync tells the worker a person asked for it`() {
        assertEquals(
            SyncTrigger.Manual,
            immediateSyncRequest().workSpec.input.syncTrigger(),
        )
    }

    @Test
    fun `an immediate sync waits for nothing`() {
        val spec = immediateSyncRequest().workSpec

        assertEquals(0L, spec.initialDelay)
        assertEquals(NetworkType.NOT_REQUIRED, spec.constraints.requiredNetworkType)
    }

    @Test
    fun `both requests target the sync worker`() {
        assertEquals(
            FiatRatesSyncWorker::class.java.name,
            periodicSyncRequest().workSpec.workerClassName,
        )
        assertEquals(
            FiatRatesSyncWorker::class.java.name,
            immediateSyncRequest().workSpec.workerClassName,
        )
    }
}
