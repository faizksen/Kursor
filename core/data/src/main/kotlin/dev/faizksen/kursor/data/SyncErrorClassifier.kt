package dev.faizksen.kursor.data

import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.network.model.RemoteRateFormatException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import io.ktor.client.network.sockets.SocketTimeoutException as KtorSocketTimeoutException

internal fun Throwable.toSyncOutcome(): SyncOutcome = when (this) {
    is UnknownHostException -> SyncOutcome.Transient(TransientKind.NoNetwork)
    is HttpRequestTimeoutException -> SyncOutcome.Transient(TransientKind.Timeout)
    is ConnectTimeoutException -> SyncOutcome.Transient(TransientKind.Timeout)
    is KtorSocketTimeoutException -> SyncOutcome.Transient(TransientKind.Timeout)
    is SocketTimeoutException -> SyncOutcome.Transient(TransientKind.Timeout)
    is ResponseException -> toSyncOutcome()
    is SerializationException -> SyncOutcome.Permanent(PermanentKind.ParseError)
    is RemoteRateFormatException -> SyncOutcome.Permanent(PermanentKind.ParseError)
    is IOException -> SyncOutcome.Transient(TransientKind.NoNetwork)
    else -> SyncOutcome.Permanent(PermanentKind.Unknown(this))
}

private fun ResponseException.toSyncOutcome(): SyncOutcome = when (response.status.value) {
    TOO_MANY_REQUESTS -> SyncOutcome.Transient(TransientKind.RateLimited(retryAfter()))
    in SERVER_ERRORS -> SyncOutcome.Transient(TransientKind.ServerError)
    else -> SyncOutcome.Permanent(PermanentKind.InvalidCurrency)
}

private fun ResponseException.retryAfter(): Duration? = response
    .headers[HttpHeaders.RetryAfter]
    ?.toLongOrNull()
    ?.seconds

private const val TOO_MANY_REQUESTS = 429
private val SERVER_ERRORS = 500..599
