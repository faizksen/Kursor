package dev.faizksen.kursor.sync

import androidx.work.WorkInfo
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID
import kotlin.time.Instant

class SyncQueueMappingTest {

    @Test
    fun `no work at all is an idle queue`() {
        assertEquals(SyncQueue.Idle, null.toSyncQueue())
    }

    @Test
    fun `running work is reported as running`() {
        assertEquals(SyncState.Running, workInfo(WorkInfo.State.RUNNING).toSyncQueue().state)
    }

    @Test
    fun `enqueued and blocked work are both reported as scheduled`() {
        assertEquals(SyncState.Scheduled, workInfo(WorkInfo.State.ENQUEUED).toSyncQueue().state)
        assertEquals(SyncState.Scheduled, workInfo(WorkInfo.State.BLOCKED).toSyncQueue().state)
    }

    @Test
    fun `finished work keeps its outcome`() {
        assertEquals(SyncState.Succeeded, workInfo(WorkInfo.State.SUCCEEDED).toSyncQueue().state)
        assertEquals(SyncState.Failed, workInfo(WorkInfo.State.FAILED).toSyncQueue().state)
    }

    @Test
    fun `cancelled work is indistinguishable from no work`() {
        assertEquals(SyncState.Idle, workInfo(WorkInfo.State.CANCELLED).toSyncQueue().state)
    }

    @Test
    fun `the attempt count is carried over`() {
        assertEquals(
            2,
            workInfo(WorkInfo.State.ENQUEUED, runAttemptCount = 2).toSyncQueue().attemptCount,
        )
    }

    @Test
    fun `enqueued work reports when it runs next`() {
        assertEquals(
            Instant.fromEpochMilliseconds(NEXT_RUN_MILLIS),
            workInfo(WorkInfo.State.ENQUEUED, nextScheduleTimeMillis = NEXT_RUN_MILLIS)
                .toSyncQueue()
                .nextRunAt,
        )
    }

    @Test
    fun `work with no schedule ahead of it reports no next run`() {
        assertNull(
            workInfo(WorkInfo.State.ENQUEUED, nextScheduleTimeMillis = Long.MAX_VALUE)
                .toSyncQueue()
                .nextRunAt,
        )
    }

    @Test
    fun `work that is not waiting reports no next run`() {
        assertNull(
            workInfo(WorkInfo.State.RUNNING, nextScheduleTimeMillis = NEXT_RUN_MILLIS)
                .toSyncQueue()
                .nextRunAt,
        )
    }

    @Test
    fun `a queue waiting after a failed attempt is a retry`() {
        assertEquals(
            true,
            workInfo(WorkInfo.State.ENQUEUED, runAttemptCount = 1)
                .toSyncQueue()
                .isWaitingForRetry,
        )
    }

    @Test
    fun `a queue waiting for its first run is not a retry`() {
        assertEquals(
            false,
            workInfo(WorkInfo.State.ENQUEUED, runAttemptCount = 0)
                .toSyncQueue()
                .isWaitingForRetry,
        )
    }

    private fun workInfo(
        state: WorkInfo.State,
        runAttemptCount: Int = 0,
        nextScheduleTimeMillis: Long = Long.MAX_VALUE,
    ) = WorkInfo(
        id = UUID.randomUUID(),
        state = state,
        tags = emptySet(),
        runAttemptCount = runAttemptCount,
        nextScheduleTimeMillis = nextScheduleTimeMillis,
    )

    private companion object {
        const val NEXT_RUN_MILLIS = 1_793_000_000_000L
    }
}
