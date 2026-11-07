package dev.faizksen.kursor.data

import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.network.model.RemoteSourceException

internal fun Throwable.toSyncOutcome(): SyncOutcome = when (this) {
    is RemoteSourceException -> toSyncOutcome()
    else -> SyncOutcome.Permanent(PermanentKind.Unknown(this))
}

private fun RemoteSourceException.toSyncOutcome(): SyncOutcome = when (this) {
    is RemoteSourceException.NoNetwork -> SyncOutcome.Transient(TransientKind.NoNetwork)
    is RemoteSourceException.Timeout -> SyncOutcome.Transient(TransientKind.Timeout)
    is RemoteSourceException.ServerError -> SyncOutcome.Transient(TransientKind.ServerError)
    is RemoteSourceException.RateLimited -> SyncOutcome.Transient(TransientKind.RateLimited(retryAfter))
    is RemoteSourceException.Rejected -> SyncOutcome.Permanent(PermanentKind.InvalidCurrency)
    is RemoteSourceException.Malformed -> SyncOutcome.Permanent(PermanentKind.ParseError)
}
