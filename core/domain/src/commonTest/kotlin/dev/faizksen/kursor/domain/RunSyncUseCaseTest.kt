package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.data.SyncLogRepository
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncErrorKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class RunSyncUseCaseTest {

    private var outcome: SyncOutcome = SyncOutcome.Success(3)
    private var refreshedSource: SourceId? = null

    private val recorded = mutableListOf<SyncRun>()
    private var recordFailure: Throwable? = null

    private var now = START

    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    private val ratesRepository = object : RatesRepository {
        override fun observeWatchlist(): Flow<List<WatchedPairWithRate>> = emptyFlow()

        override suspend fun refresh(sourceId: SourceId): SyncOutcome {
            refreshedSource = sourceId
            now += 2.seconds
            return outcome
        }
    }

    private val syncLogRepository = object : SyncLogRepository {
        override fun observeRecent(limit: Int): Flow<List<SyncRun>> = emptyFlow()

        override fun observeLastSuccess(): Flow<SyncRun?> = emptyFlow()

        override suspend fun record(run: SyncRun): Long {
            recordFailure?.let { throw it }
            recorded += run
            return recorded.size.toLong()
        }

        override suspend fun deleteOlderThan(threshold: Instant) = Unit
    }

    private val useCase = RunSyncUseCase(
        refreshRates = RefreshRatesUseCase(ratesRepository),
        recordSyncRun = RecordSyncRunUseCase(syncLogRepository),
        clock = clock,
    )

    @Test
    fun `the outcome of the refresh is returned`() = runTest {
        outcome = SyncOutcome.Success(7)

        assertEquals(SyncOutcome.Success(7), run())
    }

    @Test
    fun `the requested source is the one refreshed`() = runTest {
        run(sourceId = SourceId.CBR)

        assertEquals(SourceId.CBR, refreshedSource)
    }

    @Test
    fun `the run is recorded against the same source`() = runTest {
        run(sourceId = SourceId.CBR)

        assertEquals(SourceId.CBR, assertNotNull(recorded.singleOrNull()).sourceId)
    }

    @Test
    fun `the recorded run spans the refresh`() = runTest {
        run()

        val recordedRun = assertNotNull(recorded.singleOrNull())
        assertEquals(START, recordedRun.startedAt)
        assertEquals(START + 2.seconds, recordedRun.finishedAt)
    }

    @Test
    fun `a successful run is recorded as success`() = runTest {
        outcome = SyncOutcome.Success(3)

        run()

        val recordedRun = assertNotNull(recorded.singleOrNull())
        assertEquals(SyncResult.Success, recordedRun.result)
        assertNull(recordedRun.errorKind)
        assertEquals(3, recordedRun.updatedCount)
    }

    @Test
    fun `a transient failure is recorded as retry while attempts remain`() = runTest {
        outcome = SyncOutcome.Transient(TransientKind.NoNetwork)

        run(canRetry = true)

        val recordedRun = assertNotNull(recorded.singleOrNull())
        assertEquals(SyncResult.Retry, recordedRun.result)
        assertEquals(SyncErrorKind.NoNetwork, recordedRun.errorKind)
    }

    @Test
    fun `a transient failure on the last attempt is recorded as failed`() = runTest {
        outcome = SyncOutcome.Transient(TransientKind.NoNetwork)

        run(canRetry = false)

        assertEquals(SyncResult.Failed, assertNotNull(recorded.singleOrNull()).result)
    }

    @Test
    fun `the trigger and the attempt are recorded`() = runTest {
        run(trigger = SyncTrigger.Manual, attempt = 2)

        val recordedRun = assertNotNull(recorded.singleOrNull())
        assertEquals(SyncTrigger.Manual, recordedRun.trigger)
        assertEquals(2, recordedRun.attempt)
    }

    @Test
    fun `a failure to record does not fail the sync`() = runTest {
        recordFailure = IllegalStateException("database is gone")
        outcome = SyncOutcome.Success(1)

        assertEquals(SyncOutcome.Success(1), run())
        assertTrue(recorded.isEmpty())
    }

    @Test
    fun `cancellation while recording is not swallowed`() = runTest {
        recordFailure = kotlinx.coroutines.CancellationException("stopped")

        assertFailsWith<kotlinx.coroutines.CancellationException> { run() }
    }

    @Test
    fun `a permanent failure is recorded and returned`() = runTest {
        outcome = SyncOutcome.Permanent(PermanentKind.ParseError)

        assertEquals(SyncOutcome.Permanent(PermanentKind.ParseError), run())
        assertEquals(SyncErrorKind.ParseError, assertNotNull(recorded.singleOrNull()).errorKind)
    }

    private suspend fun run(
        sourceId: SourceId = SourceId.FRANKFURTER,
        trigger: SyncTrigger = SyncTrigger.Periodic,
        attempt: Int = 1,
        canRetry: Boolean = true,
    ): SyncOutcome = useCase(
        sourceId = sourceId,
        trigger = trigger,
        attempt = attempt,
        canRetry = canRetry,
    )

    private companion object {
        val START = Instant.parse("2026-10-13T10:00:00Z")
    }
}
