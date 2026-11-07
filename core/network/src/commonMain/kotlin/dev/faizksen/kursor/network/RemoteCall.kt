package dev.faizksen.kursor.network

import dev.faizksen.kursor.network.model.RemoteSourceException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpHeaders
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal suspend fun <T> remoteCall(block: suspend () -> T): T = try {
    block()
} catch (e: HttpRequestTimeoutException) {
    throw RemoteSourceException.Timeout(e)
} catch (e: ConnectTimeoutException) {
    throw RemoteSourceException.Timeout(e)
} catch (e: SocketTimeoutException) {
    throw RemoteSourceException.Timeout(e)
} catch (e: ResponseException) {
    throw e.toRemoteSourceException()
} catch (e: ContentConvertException) {
    throw RemoteSourceException.Malformed(e)
} catch (e: SerializationException) {
    throw RemoteSourceException.Malformed(e)
} catch (e: IOException) {
    throw RemoteSourceException.NoNetwork(e)
}

private fun ResponseException.toRemoteSourceException(): RemoteSourceException {
    val status = response.status.value

    return when {
        status == TOO_MANY_REQUESTS -> RemoteSourceException.RateLimited(retryAfter(), this)
        status in SERVER_ERRORS -> RemoteSourceException.ServerError(status, this)
        else -> RemoteSourceException.Rejected(status, this)
    }
}

private fun ResponseException.retryAfter(): Duration? = response
    .headers[HttpHeaders.RetryAfter]
    ?.toLongOrNull()
    ?.seconds

private const val TOO_MANY_REQUESTS = 429
private val SERVER_ERRORS = 500..599
