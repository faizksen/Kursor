package dev.faizksen.kursor.watchlist

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncErrorKind
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import org.junit.Test
import java.time.Instant

class LastUpdateTest {

    private val finishedAt = Instant.parse("2026-10-13T10:00:02Z")

    @Test
    fun `empty log without running work means never updated`() {
        assertThat(lastUpdate(lastRun = null, syncState = SyncState.Idle))
            .isEqualTo(LastUpdate.Empty)
    }

    @Test
    fun `running work shows progress`() {
        assertThat(lastUpdate(lastRun = null, syncState = SyncState.Running))
            .isEqualTo(LastUpdate.Progress(0))
    }

    @Test
    fun `successful run shows when it finished`() {
        val lastRun = syncRun(result = SyncResult.Success)

        assertThat(lastUpdate(lastRun, SyncState.Succeeded))
            .isEqualTo(LastUpdate.Success(finishedAt))
    }

    @Test
    fun `transient failure promises another attempt`() {
        val lastRun = syncRun(result = SyncResult.Retry, errorKind = SyncErrorKind.NoNetwork)

        assertThat(lastUpdate(lastRun, SyncState.Idle)).isEqualTo(
            LastUpdate.Failed(failedAttempts = 2, lastTryAt = finishedAt, willRetry = true)
        )
    }

    @Test
    fun `permanent failure promises nothing`() {
        val lastRun = syncRun(result = SyncResult.Failed, errorKind = SyncErrorKind.ParseError)

        assertThat(lastUpdate(lastRun, SyncState.Failed)).isEqualTo(
            LastUpdate.Failed(failedAttempts = 2, lastTryAt = finishedAt, willRetry = false)
        )
    }

    @Test
    fun `work waiting for a retry keeps showing the failure`() {
        listOf(SyncResult.Retry, SyncResult.Failed).forEach { result ->
            val lastUpdate = lastUpdate(syncRun(result = result), SyncState.Scheduled)

            assertWithMessage("result $result").that(lastUpdate).isInstanceOf(LastUpdate.Failed::class.java)
        }
    }

    @Test
    fun `work scheduled after a successful run shows progress`() {
        val lastRun = syncRun(result = SyncResult.Success)

        assertThat(lastUpdate(lastRun, SyncState.Scheduled)).isEqualTo(LastUpdate.Progress(0))
    }

    @Test
    fun `running work wins over the previous failure`() {
        val lastRun = syncRun(result = SyncResult.Retry)

        assertThat(lastUpdate(lastRun, SyncState.Running)).isEqualTo(LastUpdate.Progress(0))
    }

    @Test
    fun `attempt number comes from the log`() {
        val lastRun = syncRun(result = SyncResult.Retry, attempt = 7)

        assertThat((lastUpdate(lastRun, SyncState.Idle) as LastUpdate.Failed).failedAttempts)
            .isEqualTo(7)
    }

    private fun syncRun(
        result: SyncResult,
        errorKind: SyncErrorKind? = null,
        attempt: Int = 2,
    ) = SyncRun(
        id = 1,
        sourceId = SourceId.FRANKFURTER,
        trigger = SyncTrigger.Manual,
        startedAt = Instant.parse("2026-10-13T10:00:00Z"),
        finishedAt = finishedAt,
        result = result,
        errorKind = errorKind,
        updatedCount = null,
        attempt = attempt,
    )
}
