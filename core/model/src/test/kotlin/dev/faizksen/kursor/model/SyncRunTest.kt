package dev.faizksen.kursor.model

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.time.Duration
import java.time.Instant

class SyncRunTest {

    private val startedAt = Instant.parse("2026-10-13T10:00:00Z")
    private val finishedAt = Instant.parse("2026-10-13T10:00:02Z")

    @Test
    fun `successful outcome keeps updated count and has no error`() {
        val run = SyncOutcome.Success(updatedCount = 7).toRun()

        assertThat(run.result).isEqualTo(SyncResult.Success)
        assertThat(run.errorKind).isNull()
        assertThat(run.updatedCount).isEqualTo(7)
    }

    @Test
    fun `transient outcome is recorded as retry`() {
        val run = SyncOutcome.Transient(TransientKind.Timeout).toRun()

        assertThat(run.result).isEqualTo(SyncResult.Retry)
        assertThat(run.errorKind).isEqualTo(SyncErrorKind.Timeout)
        assertThat(run.updatedCount).isNull()
    }

    @Test
    fun `permanent outcome is recorded as failed`() {
        val run = SyncOutcome.Permanent(PermanentKind.ParseError).toRun()

        assertThat(run.result).isEqualTo(SyncResult.Failed)
        assertThat(run.errorKind).isEqualTo(SyncErrorKind.ParseError)
        assertThat(run.updatedCount).isNull()
    }

    @Test
    fun `every transient kind maps to its own error kind`() {
        val expected = listOf(
            TransientKind.NoNetwork to SyncErrorKind.NoNetwork,
            TransientKind.Timeout to SyncErrorKind.Timeout,
            TransientKind.ServerError to SyncErrorKind.ServerError,
            TransientKind.RateLimited(Duration.ofSeconds(30)) to SyncErrorKind.RateLimited,
        )

        expected.forEach { (kind, errorKind) ->
            assertWithMessage("transient $kind")
                .that(SyncOutcome.Transient(kind).toRun().errorKind)
                .isEqualTo(errorKind)
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
            assertWithMessage("permanent $kind")
                .that(SyncOutcome.Permanent(kind).toRun().errorKind)
                .isEqualTo(errorKind)
        }
    }

    @Test
    fun `transient outcome on the last attempt is recorded as failed`() {
        val run = SyncOutcome.Transient(TransientKind.NoNetwork).toRun(canRetry = false)

        assertThat(run.result).isEqualTo(SyncResult.Failed)
        assertThat(run.errorKind).isEqualTo(SyncErrorKind.NoNetwork)
    }

    @Test
    fun `successful outcome is not affected by the attempt limit`() {
        val run = SyncOutcome.Success(updatedCount = 1).toRun(canRetry = false)

        assertThat(run.result).isEqualTo(SyncResult.Success)
    }

    @Test
    fun `rate limited loses its retry after`() {
        val withDelay = SyncOutcome.Transient(TransientKind.RateLimited(Duration.ofSeconds(30)))
        val withoutDelay = SyncOutcome.Transient(TransientKind.RateLimited(null))

        assertThat(withDelay.toRun().errorKind).isEqualTo(withoutDelay.toRun().errorKind)
    }

    @Test
    fun `run carries the context it was given`() {
        val run = SyncOutcome.Success(updatedCount = 3).toRun()

        assertThat(run.sourceId).isEqualTo(SourceId.FRANKFURTER)
        assertThat(run.trigger).isEqualTo(SyncTrigger.Manual)
        assertThat(run.startedAt).isEqualTo(startedAt)
        assertThat(run.finishedAt).isEqualTo(finishedAt)
        assertThat(run.attempt).isEqualTo(2)
    }

    @Test
    fun `fresh run is not stored yet`() {
        assertThat(SyncOutcome.Success(updatedCount = 0).toRun().id)
            .isEqualTo(SyncRun.NOT_STORED)
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
