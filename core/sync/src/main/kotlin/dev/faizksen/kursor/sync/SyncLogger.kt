package dev.faizksen.kursor.sync

import dev.faizksen.kursor.domain.RecordSyncRunUseCase
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.model.toSyncRun
import kotlinx.coroutines.CancellationException
import java.time.Clock
import javax.inject.Inject

class SyncLogger @Inject constructor(
    private val recordSyncRun: RecordSyncRunUseCase,
    private val clock: Clock,
) {
    suspend fun log(
        sourceId: SourceId,
        trigger: SyncTrigger,
        attempt: Int,
        canRetry: Boolean,
        block: suspend () -> SyncOutcome,
    ): SyncOutcome {
        val startedAt = clock.instant()
        val outcome = block()

        record(
            outcome.toSyncRun(
                sourceId = sourceId,
                trigger = trigger,
                startedAt = startedAt,
                finishedAt = clock.instant(),
                attempt = attempt,
                canRetry = canRetry,
            )
        )

        return outcome
    }

    private suspend fun record(run: SyncRun) {
        try {
            recordSyncRun(run)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (ignored: Throwable) {
        }
    }
}
