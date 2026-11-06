package dev.faizksen.kursor.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlin.time.Duration.Companion.seconds

class SyncRunTest {

    private val startedAt = Instant.parse("2026-10-13T10:00:00Z")
    private val finishedAt = Instant.parse("2026-10-13T10:00:02Z")

    @Test
    fun `successful outcome keeps updated count and has no error`() {
        val run = SyncOutcome.Success(updatedCount = 7).toRun()

        assertEquals(SyncResult.Success, run.result)
        assertNull(run.errorKind)
        assertEquals(7, run.updatedCount)
    }

    @Test
    fun `transient outcome is recorded as retry`() {
        val run = SyncOutcome.Transient(TransientKind.Timeout).toRun()

        assertEquals(SyncResult.Retry, run.result)
        assertEquals(SyncErrorKind.Timeout, run.errorKind)
        assertNull(run.updatedCount)
    }

    @Test
    fun `permanent outcome is recorded as failed`() {
        val run = SyncOutcome.Permanent(PermanentKind.ParseError).toRun()

        assertEquals(SyncResult.Failed, run.result)
        assertEquals(SyncErrorKind.ParseError, run.errorKind)
        assertNull(run.updatedCount)
    }

    @Test
    fun `every transient kind maps to its own error kind`() {
        val expected = listOf(
            TransientKind.NoNetwork to SyncErrorKind.NoNetwork,
            TransientKind.Timeout to SyncErrorKind.Timeout,
            TransientKind.ServerError to SyncErrorKind.ServerError,
            TransientKind.RateLimited(30.seconds) to SyncErrorKind.RateLimited,
        )

        expected.forEach { (kind, errorKind) ->
            assertEquals(errorKind, SyncOutcome.Transient(kind).toRun().errorKind, "transient $kind")
        }
    }

    @Test
    fun `every permanent kind maps to its own error kind`() {
        val expected = listOf(
            PermanentKind.InvalidCurrency to SyncErrorKind.InvalidCurrency,
            PermanentKind.ParseError to SyncErrorKind.ParseError,
            PermanentKind.UnknownSource to SyncErrorKind.UnknownSource,
            PermanentKind.Unknown(IllegalStateException()) to SyncErrorKind.Unknown,
        )

        expected.forEach { (kind, errorKind) ->
            assertEquals(errorKind, SyncOutcome.Permanent(kind).toRun().errorKind, "permanent $kind")
        }
    }

    @Test
    fun `transient outcome on the last attempt is recorded as failed`() {
        val run = SyncOutcome.Transient(TransientKind.NoNetwork).toRun(canRetry = false)

        assertEquals(SyncResult.Failed, run.result)
        assertEquals(SyncErrorKind.NoNetwork, run.errorKind)
    }

    @Test
    fun `successful outcome is not affected by the attempt limit`() {
        val run = SyncOutcome.Success(updatedCount = 1).toRun(canRetry = false)

        assertEquals(SyncResult.Success, run.result)
    }

    @Test
    fun `rate limited loses its retry after`() {
        val withDelay = SyncOutcome.Transient(TransientKind.RateLimited(30.seconds))
        val withoutDelay = SyncOutcome.Transient(TransientKind.RateLimited(null))

        assertEquals(withoutDelay.toRun().errorKind, withDelay.toRun().errorKind)
    }

    @Test
    fun `run carries the context it was given`() {
        val run = SyncOutcome.Success(updatedCount = 3).toRun()

        assertEquals(SourceId.FRANKFURTER, run.sourceId)
        assertEquals(SyncTrigger.Manual, run.trigger)
        assertEquals(startedAt, run.startedAt)
        assertEquals(finishedAt, run.finishedAt)
        assertEquals(2, run.attempt)
    }

    @Test
    fun `fresh run is not stored yet`() {
        assertEquals(SyncRun.NOT_STORED, SyncOutcome.Success(updatedCount = 0).toRun().id)
    }

    private fun SyncOutcome.toRun(canRetry: Boolean = true): SyncRun = toSyncRun(
        sourceId = SourceId.FRANKFURTER,
        trigger = SyncTrigger.Manual,
        startedAt = startedAt,
        finishedAt = finishedAt,
        attempt = 2,
        canRetry = canRetry,
    )
}
