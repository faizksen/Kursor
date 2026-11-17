package dev.faizksen.kursor.settings.rundetail

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncErrorKind
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncTrigger
import org.junit.Test
import kotlin.time.Instant

class RunDetailTest {

    @Test
    fun `a run that was pruned while the screen was open says so`() {
        assertThat(runDetail(run = null, attempts = listOf(attempt(NBP))))
            .isEqualTo(RunDetailUiState.Gone)
    }

    @Test
    fun `a run shows every issuer it asked`() {
        val attempts = listOf(attempt(NBP), attempt(ECB))

        assertThat(runDetail(run = run(), attempts = attempts))
            .isEqualTo(RunDetailUiState.Content(run = run(), attempts = attempts))
    }

    @Test
    fun `a run recorded before attempts were kept shows none`() {
        assertThat(runDetail(run = run(), attempts = emptyList()))
            .isEqualTo(RunDetailUiState.Content(run = run(), attempts = emptyList()))
    }

    private fun run() = SyncRun(
        id = 1,
        sourceId = SourceId.FRANKFURTER,
        trigger = SyncTrigger.Periodic,
        startedAt = START,
        finishedAt = START,
        result = SyncResult.Success,
        errorKind = null,
        updatedCount = 1,
        requestedCount = 2,
        attempt = 1,
    )

    private fun attempt(
        provider: ProviderKey,
        errorKind: SyncErrorKind? = null,
    ) = ProviderAttempt(
        provider = provider,
        requestedCount = 1,
        coveredCount = if (errorKind == null) 1 else 0,
        errorKind = errorKind,
        startedAt = START,
        finishedAt = START,
    )

    private companion object {
        val NBP = ProviderKey("NBP")
        val ECB = ProviderKey("ECB")
        val START = Instant.fromEpochMilliseconds(1_000L)
    }
}
