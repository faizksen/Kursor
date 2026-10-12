package dev.faizksen.kursor.data

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import kotlinx.serialization.SerializationException
import okhttp3.Headers
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Duration
import java.time.format.DateTimeParseException

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
    fun dateTimeParseExceptionIsPermanentParseError() {
        val exception = DateTimeParseException("", "", 0)
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
                TransientKind.RateLimited(Duration.ofSeconds(3600))
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

    private fun httpException(code: Int, retryAfter: String? = null): HttpException {
        val headers = retryAfter?.let { Headers.headersOf("Retry-After", it) } ?: Headers.headersOf()
        val response = Response.error<Any>(
            "".toResponseBody(),
            okhttp3.Response.Builder()
                .code(code)
                .message("")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .headers(headers)
                .build(),
        )
        return HttpException(response)
    }
}