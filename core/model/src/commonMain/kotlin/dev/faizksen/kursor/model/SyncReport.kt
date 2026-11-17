package dev.faizksen.kursor.model

import kotlin.time.Instant

enum class AttemptResult {
    Answered,
    Empty,
    Failed,
}

data class ProviderAttempt(
    val provider: ProviderKey?,
    val requestedCount: Int,
    val coveredCount: Int,
    val errorKind: SyncErrorKind?,
    val startedAt: Instant,
    val finishedAt: Instant,
    val id: Long = NOT_STORED,
    val runId: Long = NOT_STORED,
) {
    val result: AttemptResult
        get() = when {
            errorKind != null -> AttemptResult.Failed
            coveredCount == 0 -> AttemptResult.Empty
            else -> AttemptResult.Answered
        }

    companion object {
        const val NOT_STORED = 0L
    }
}

data class SyncReport(
    val attempts: List<ProviderAttempt>,
    val firstFailure: SyncOutcome? = null,
) {
    val requestedCount: Int get() = attempts.sumOf { attempt -> attempt.requestedCount }

    val coveredCount: Int get() = attempts.sumOf { attempt -> attempt.coveredCount }

    val outcome: SyncOutcome
        get() = when {
            coveredCount > 0 -> SyncOutcome.Success(coveredCount, requestedCount)
            firstFailure != null -> firstFailure
            else -> SyncOutcome.Success(0, requestedCount)
        }
}
