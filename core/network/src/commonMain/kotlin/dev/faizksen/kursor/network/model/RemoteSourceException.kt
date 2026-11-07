package dev.faizksen.kursor.network.model

import kotlin.time.Duration

sealed class RemoteSourceException(cause: Throwable?) : Exception(cause) {
    class NoNetwork(cause: Throwable?) : RemoteSourceException(cause)

    class Timeout(cause: Throwable?) : RemoteSourceException(cause)

    class ServerError(val status: Int, cause: Throwable?) : RemoteSourceException(cause)

    class RateLimited(val retryAfter: Duration?, cause: Throwable?) : RemoteSourceException(cause)

    class Rejected(val status: Int, cause: Throwable?) : RemoteSourceException(cause)

    class Malformed(cause: Throwable?) : RemoteSourceException(cause)
}
