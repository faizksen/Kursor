package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.model.toSyncRun
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock

class RunSyncUseCase(
    private val refreshRates: RefreshRatesUseCase,
    private val recordSyncRun: RecordSyncRunUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        sourceId: SourceId,
        trigger: SyncTrigger,
        attempt: Int,
        canRetry: Boolean,
        only: CurrencyPair? = null,
    ): SyncOutcome {
        val startedAt = clock.now()
        val report = refreshRates(sourceId, only)
        val outcome = report.outcome

        record(
            run = outcome.toSyncRun(
                sourceId = sourceId,
                trigger = trigger,
                startedAt = startedAt,
                finishedAt = clock.now(),
                attempt = attempt,
                canRetry = canRetry,
            ),
            attempts = report.attempts,
        )

        return outcome
    }

    private suspend fun record(run: SyncRun, attempts: List<ProviderAttempt>) {
        try {
            recordSyncRun(run, attempts)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (ignored: Throwable) {
        }
    }
}
