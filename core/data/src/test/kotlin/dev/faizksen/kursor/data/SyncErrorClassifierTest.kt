package dev.faizksen.kursor.data

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.network.model.RemoteSourceException
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class SyncErrorClassifierTest {

    @Test
    fun noNetworkIsTransient() {
        val result = RemoteSourceException.NoNetwork(cause = null).toSyncOutcome()

        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.NoNetwork))
    }

    @Test
    fun timeoutIsTransient() {
        val result = RemoteSourceException.Timeout(cause = null).toSyncOutcome()

        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.Timeout))
    }

    @Test
    fun serverErrorIsTransient() {
        val result = RemoteSourceException.ServerError(status = 500, cause = null).toSyncOutcome()

        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.ServerError))
    }

    @Test
    fun rateLimitedKeepsItsRetryAfter() {
        val result = RemoteSourceException
            .RateLimited(retryAfter = 3600.seconds, cause = null)
            .toSyncOutcome()

        assertThat(result).isEqualTo(
            SyncOutcome.Transient(TransientKind.RateLimited(3600.seconds))
        )
    }

    @Test
    fun rateLimitedWithoutRetryAfterIsStillTransient() {
        val result = RemoteSourceException
            .RateLimited(retryAfter = null, cause = null)
            .toSyncOutcome()

        assertThat(result).isEqualTo(SyncOutcome.Transient(TransientKind.RateLimited(null)))
    }

    @Test
    fun rejectedRequestIsPermanent() {
        val result = RemoteSourceException.Rejected(status = 404, cause = null).toSyncOutcome()

        assertThat(result).isEqualTo(SyncOutcome.Permanent(PermanentKind.InvalidCurrency))
    }

    @Test
    fun malformedResponseIsPermanentParseError() {
        val result = RemoteSourceException.Malformed(cause = null).toSyncOutcome()

        assertThat(result).isEqualTo(SyncOutcome.Permanent(PermanentKind.ParseError))
    }

    @Test
    fun unexpectedExceptionIsPermanentUnknown() {
        val exception = IllegalStateException()

        val result = exception.toSyncOutcome()

        assertThat(result).isEqualTo(SyncOutcome.Permanent(PermanentKind.Unknown(exception)))
    }
}
