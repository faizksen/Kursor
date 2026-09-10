package dev.faizksen.kursor.data.model

import java.time.Duration

sealed interface SyncOutcome {
    data class Success(val updatedCount: Int) : SyncOutcome
    data class Transient(val kind: TransientKind) : SyncOutcome
    data class Permanent(val kind: PermanentKind) : SyncOutcome
}

sealed interface TransientKind {
    data object NoNetwork : TransientKind
    data object Timeout : TransientKind
    data object ServerError : TransientKind
    data class RateLimited(val retryAfter: Duration?) : TransientKind
}

sealed interface PermanentKind {
    data object InvalidCurrency : PermanentKind
    data object ParseError : PermanentKind
    data object UnknownSource : PermanentKind
    data class Unknown(val throwable: Throwable): PermanentKind
}
