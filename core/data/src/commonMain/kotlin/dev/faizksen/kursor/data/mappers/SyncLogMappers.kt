package dev.faizksen.kursor.data.mappers

import dev.faizksen.kursor.database.consts.AttemptResultColumn
import dev.faizksen.kursor.database.consts.ErrorColumn
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.consts.SyncResultColumn
import dev.faizksen.kursor.database.consts.TriggerColumn
import dev.faizksen.kursor.database.entities.SyncAttemptEntity
import dev.faizksen.kursor.database.entities.SyncLogEntity
import dev.faizksen.kursor.model.AttemptResult
import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncErrorKind
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncTrigger
import kotlin.time.Instant

internal fun SyncRun.toSyncLogEntity() = SyncLogEntity(
    id = id,
    sourceId = sourceId.toColumn(),
    trigger = trigger.toColumn(),
    startedAt = startedAt.toEpochMilliseconds(),
    finishedAt = finishedAt.toEpochMilliseconds(),
    result = result.toColumn(),
    errorKind = errorKind?.toColumn(),
    updatedCount = updatedCount,
    requestedCount = requestedCount,
    attempt = attempt,
)

internal fun SyncLogEntity.toSyncRun(): SyncRun? {
    val source = sourceId.toSourceId() ?: return null
    val syncTrigger = trigger.toSyncTrigger() ?: return null
    val syncResult = result.toSyncResult() ?: return null

    return SyncRun(
        id = id,
        sourceId = source,
        trigger = syncTrigger,
        startedAt = Instant.fromEpochMilliseconds(startedAt),
        finishedAt = Instant.fromEpochMilliseconds(finishedAt),
        result = syncResult,
        errorKind = errorKind?.toSyncErrorKind(),
        updatedCount = updatedCount,
        requestedCount = requestedCount,
        attempt = attempt,
    )
}

internal fun SourceId.toColumn(): SourceIdColumn = when (this) {
    SourceId.FRANKFURTER -> SourceIdColumn.Frankfurter
    SourceId.COINGECKO -> SourceIdColumn.CoinGecko
    SourceId.CBR -> SourceIdColumn.CBR
}

internal fun SourceIdColumn.toSourceId(): SourceId? = when (this) {
    SourceIdColumn.Frankfurter -> SourceId.FRANKFURTER
    SourceIdColumn.CoinGecko -> SourceId.COINGECKO
    SourceIdColumn.CBR -> SourceId.CBR
    is SourceIdColumn.Unrecognized -> null
}

internal fun SyncTrigger.toColumn(): TriggerColumn = when (this) {
    SyncTrigger.Periodic -> TriggerColumn.Periodic
    SyncTrigger.Manual -> TriggerColumn.Manual
    SyncTrigger.PairAdded -> TriggerColumn.PairAdded
}

internal fun TriggerColumn.toSyncTrigger(): SyncTrigger? = when (this) {
    TriggerColumn.Periodic -> SyncTrigger.Periodic
    TriggerColumn.Manual -> SyncTrigger.Manual
    TriggerColumn.PairAdded -> SyncTrigger.PairAdded
    is TriggerColumn.Unrecognized -> null
}

internal fun SyncResult.toColumn(): SyncResultColumn = when (this) {
    SyncResult.Success -> SyncResultColumn.Success
    SyncResult.Retry -> SyncResultColumn.Retry
    SyncResult.Failed -> SyncResultColumn.Failed
}

internal fun SyncResultColumn.toSyncResult(): SyncResult? = when (this) {
    SyncResultColumn.Success -> SyncResult.Success
    SyncResultColumn.Retry -> SyncResult.Retry
    SyncResultColumn.Failed -> SyncResult.Failed
    is SyncResultColumn.Unrecognized -> null
}

internal fun SyncErrorKind.toColumn(): ErrorColumn = when (this) {
    SyncErrorKind.NoNetwork -> ErrorColumn.NoNetwork
    SyncErrorKind.Timeout -> ErrorColumn.Timeout
    SyncErrorKind.ServerError -> ErrorColumn.ServerError
    SyncErrorKind.RateLimited -> ErrorColumn.RateLimited
    SyncErrorKind.InvalidCurrency -> ErrorColumn.InvalidCurrency
    SyncErrorKind.ParseError -> ErrorColumn.ParseError
    SyncErrorKind.UnknownSource -> ErrorColumn.UnknownSource
    SyncErrorKind.Unknown -> ErrorColumn.Unknown
}

internal fun ErrorColumn.toSyncErrorKind(): SyncErrorKind = when (this) {
    ErrorColumn.NoNetwork -> SyncErrorKind.NoNetwork
    ErrorColumn.Timeout -> SyncErrorKind.Timeout
    ErrorColumn.ServerError -> SyncErrorKind.ServerError
    ErrorColumn.RateLimited -> SyncErrorKind.RateLimited
    ErrorColumn.InvalidCurrency -> SyncErrorKind.InvalidCurrency
    ErrorColumn.ParseError -> SyncErrorKind.ParseError
    ErrorColumn.UnknownSource -> SyncErrorKind.UnknownSource
    ErrorColumn.Unknown -> SyncErrorKind.Unknown
    is ErrorColumn.Unrecognized -> SyncErrorKind.Unknown
}

internal fun ProviderAttempt.toEntity(runId: Long) = SyncAttemptEntity(
    runId = runId,
    provider = provider?.key.orEmpty(),
    result = result.toColumn(),
    errorKind = errorKind?.toColumn(),
    requestedCount = requestedCount,
    coveredCount = coveredCount,
    startedAt = startedAt.toEpochMilliseconds(),
    finishedAt = finishedAt.toEpochMilliseconds(),
)

internal fun SyncAttemptEntity.toProviderAttempt(): ProviderAttempt = ProviderAttempt(
    id = id,
    runId = runId,
    provider = provider.takeIf { key -> key.isNotBlank() }?.let(::ProviderKey),
    requestedCount = requestedCount,
    coveredCount = coveredCount,
    errorKind = errorKind?.toSyncErrorKind(),
    startedAt = Instant.fromEpochMilliseconds(startedAt),
    finishedAt = Instant.fromEpochMilliseconds(finishedAt),
)

internal fun AttemptResult.toColumn(): AttemptResultColumn = when (this) {
    AttemptResult.Answered -> AttemptResultColumn.Answered
    AttemptResult.Empty -> AttemptResultColumn.Empty
    AttemptResult.Failed -> AttemptResultColumn.Failed
}

internal fun AttemptResultColumn.toAttemptResult(): AttemptResult? = when (this) {
    AttemptResultColumn.Answered -> AttemptResult.Answered
    AttemptResultColumn.Empty -> AttemptResult.Empty
    AttemptResultColumn.Failed -> AttemptResult.Failed
    is AttemptResultColumn.Unrecognized -> null
}
