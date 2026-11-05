package dev.faizksen.kursor.data

import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.network.model.RemoteRateFormatException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun Throwable.toSyncOutcome(): SyncOutcome = when (this) {
    is UnknownHostException -> SyncOutcome.Transient(TransientKind.NoNetwork)
    is SocketTimeoutException -> SyncOutcome.Transient(TransientKind.Timeout)
    is HttpException -> toSyncOutcome()
    is SerializationException -> SyncOutcome.Permanent(PermanentKind.ParseError)
    is RemoteRateFormatException -> SyncOutcome.Permanent(PermanentKind.ParseError)
    is IOException -> SyncOutcome.Transient(TransientKind.NoNetwork)
    else -> SyncOutcome.Permanent(PermanentKind.Unknown(this))
}

private fun HttpException.toSyncOutcome(): SyncOutcome = when (code()) {
    TOO_MANY_REQUESTS -> SyncOutcome.Transient(TransientKind.RateLimited(retryAfter()))
    in SERVER_ERRORS -> SyncOutcome.Transient(TransientKind.ServerError)
    else -> SyncOutcome.Permanent(PermanentKind.InvalidCurrency)
}

private fun HttpException.retryAfter(): Duration? = response()
    ?.headers()
    ?.get(RETRY_AFTER)
    ?.toLongOrNull()
    ?.seconds

private const val TOO_MANY_REQUESTS = 429
private const val RETRY_AFTER = "Retry-After"
private val SERVER_ERRORS = 500..599
