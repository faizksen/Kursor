package dev.faizksen.kursor.sync

import dev.faizksen.kursor.domain.RecordSyncRunUseCase
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.model.toSyncRun
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock

class SyncLogger(
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
        val startedAt = clock.now()
        val outcome = block()

        record(
            outcome.toSyncRun(
                sourceId = sourceId,
                trigger = trigger,
                startedAt = startedAt,
                finishedAt = clock.now(),
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
