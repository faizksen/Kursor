package dev.faizksen.kursor.model

import java.time.Instant

data class SyncRun(
    val sourceId: SourceId,
    val trigger: SyncTrigger,
    val startedAt: Instant,
    val finishedAt: Instant,
    val result: SyncResult,
    val errorKind: SyncErrorKind?,
    val updatedCount: Int?,
    val attempt: Int,
    val id: Long = NOT_STORED,
) {
    companion object {
        const val NOT_STORED = 0L
    }
}

enum class SyncResult {
    Success,
    Retry,
    Failed,
}

enum class SyncErrorKind {
    NoNetwork,
    Timeout,
    ServerError,
    RateLimited,
    InvalidCurrency,
    ParseError,
    UnknownSource,
    Unknown,
}

fun SyncOutcome.toSyncRun(
    sourceId: SourceId,
    trigger: SyncTrigger,
    startedAt: Instant,
    finishedAt: Instant,
    attempt: Int,
    canRetry: Boolean,
): SyncRun = SyncRun(
    sourceId = sourceId,
    trigger = trigger,
    startedAt = startedAt,
    finishedAt = finishedAt,
    result = toSyncResult(canRetry),
    errorKind = toSyncErrorKind(),
    updatedCount = (this as? SyncOutcome.Success)?.updatedCount,
    attempt = attempt,
)

private fun SyncOutcome.toSyncResult(canRetry: Boolean): SyncResult = when (this) {
    is SyncOutcome.Success -> SyncResult.Success
    is SyncOutcome.Transient -> if (canRetry) SyncResult.Retry else SyncResult.Failed
    is SyncOutcome.Permanent -> SyncResult.Failed
}

private fun SyncOutcome.toSyncErrorKind(): SyncErrorKind? = when (this) {
    is SyncOutcome.Success -> null
    is SyncOutcome.Transient -> kind.toSyncErrorKind()
    is SyncOutcome.Permanent -> kind.toSyncErrorKind()
}

private fun TransientKind.toSyncErrorKind(): SyncErrorKind = when (this) {
    TransientKind.NoNetwork -> SyncErrorKind.NoNetwork
    TransientKind.Timeout -> SyncErrorKind.Timeout
    TransientKind.ServerError -> SyncErrorKind.ServerError
    is TransientKind.RateLimited -> SyncErrorKind.RateLimited
}

private fun PermanentKind.toSyncErrorKind(): SyncErrorKind = when (this) {
    PermanentKind.InvalidCurrency -> SyncErrorKind.InvalidCurrency
    PermanentKind.ParseError -> SyncErrorKind.ParseError
    PermanentKind.UnknownSource -> SyncErrorKind.UnknownSource
    is PermanentKind.Unknown -> SyncErrorKind.Unknown
}
