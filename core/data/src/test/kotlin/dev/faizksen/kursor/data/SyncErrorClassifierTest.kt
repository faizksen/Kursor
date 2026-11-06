package dev.faizksen.kursor.data

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.network.model.RemoteRateFormatException
import kotlinx.serialization.SerializationException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.seconds

class SyncErrorClassifierTest {

    @Test
    fun unknownHostIsTransientNoNetwork() {
        val exception = UnknownHostException()
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.NoNetwork))
    }

    @Test
    fun socketTimeoutIsTransientTimeout() {
        val exception = SocketTimeoutException()
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.Timeout))
    }

    @Test
    fun serializationExceptionIsPermanentParseError() {
        val exception = SerializationException()
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(SyncOutcome.Permanent(PermanentKind.ParseError))
    }

    @Test
    fun remoteRateFormatExceptionIsPermanentParseError() {
        val exception = RemoteRateFormatException(IllegalArgumentException())
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(SyncOutcome.Permanent(PermanentKind.ParseError))
    }

    @Test
    fun ioExceptionIsPermanentParseError() {
        val exception = IOException()
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.NoNetwork))
    }

    @Test
    fun unexpectedExceptionIsPermanentUnknown() {
        val exception = IllegalStateException()
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(SyncOutcome.Permanent(PermanentKind.Unknown(exception)))
    }

    @Test
    fun http429IsTransientRateLimited() {
        val exception = httpException(429, retryAfter = "3600")
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(
            SyncOutcome.Transient(
                TransientKind.RateLimited(3600.seconds)
            )
        )
    }

    @Test
    fun http429WithoutRetryAfterIsTransientRateLimited() {
        val exception = httpException(429)
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(
            SyncOutcome.Transient(
                TransientKind.RateLimited(null)
            )
        )
    }

    @Test
    fun http500IsTransientServerError() {
        val exception = httpException(500)
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.ServerError))
    }

    @Test
    fun http404IsPermanentInvalidCurrency() {
        val exception = httpException(404)
        val result = exception.toSyncOutcome()
        assertThat(result).isEqualTo(
            SyncOutcome.Permanent(PermanentKind.InvalidCurrency)
        )
    }

    private fun httpException(code: Int, retryAfter: String? = null): ResponseException = runBlocking {
        val engine = MockEngine {
            respondError(
                status = HttpStatusCode.fromValue(code),
                headers = retryAfter?.let { headersOf("Retry-After", it) } ?: headersOf(),
            )
        }
        val client = HttpClient(engine) { expectSuccess = true }

        runCatching { client.get("http://localhost/") }
            .exceptionOrNull() as ResponseException
    }
}