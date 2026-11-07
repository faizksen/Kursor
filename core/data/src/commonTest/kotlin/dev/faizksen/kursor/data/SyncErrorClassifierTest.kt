package dev.faizksen.kursor.data

import dev.faizksen.kursor.model.PermanentKind
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.model.TransientKind
import dev.faizksen.kursor.network.model.RemoteSourceException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class SyncErrorClassifierTest {

    @Test
    fun noNetworkIsTransient() {
        val result = RemoteSourceException.NoNetwork(cause = null).toSyncOutcome()

        assertEquals(SyncOutcome.Transient(TransientKind.NoNetwork), result)
    }

    @Test
    fun timeoutIsTransient() {
        val result = RemoteSourceException.Timeout(cause = null).toSyncOutcome()

        assertEquals(SyncOutcome.Transient(TransientKind.Timeout), result)
    }

    @Test
    fun serverErrorIsTransient() {
        val result = RemoteSourceException.ServerError(status = 500, cause = null).toSyncOutcome()

        assertEquals(SyncOutcome.Transient(TransientKind.ServerError), result)
    }

    @Test
    fun rateLimitedKeepsItsRetryAfter() {
        val result = RemoteSourceException
            .RateLimited(retryAfter = 3600.seconds, cause = null)
            .toSyncOutcome()

        assertEquals(SyncOutcome.Transient(TransientKind.RateLimited(3600.seconds)), result)
    }

    @Test
    fun rateLimitedWithoutRetryAfterIsStillTransient() {
        val result = RemoteSourceException
            .RateLimited(retryAfter = null, cause = null)
            .toSyncOutcome()

        assertEquals(SyncOutcome.Transient(TransientKind.RateLimited(null)), result)
    }

    @Test
    fun rejectedRequestIsPermanent() {
        val result = RemoteSourceException.Rejected(status = 404, cause = null).toSyncOutcome()

        assertEquals(SyncOutcome.Permanent(PermanentKind.InvalidCurrency), result)
    }

    @Test
    fun malformedResponseIsPermanentParseError() {
        val result = RemoteSourceException.Malformed(cause = null).toSyncOutcome()

        assertEquals(SyncOutcome.Permanent(PermanentKind.ParseError), result)
    }

    @Test
    fun unexpectedExceptionIsPermanentUnknown() {
        val exception = IllegalStateException()

        val result = exception.toSyncOutcome()

        assertEquals(SyncOutcome.Permanent(PermanentKind.Unknown(exception)), result)
    }
}
