package dev.faizksen.kursor.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SyncReportTest {

    @Test
    fun aRunWhereEveryIssuerAnsweredIsAPlainSuccess() {
        val report = SyncReport(listOf(answered(NBP, 1), answered(ECB, 2)))

        assertEquals(SyncOutcome.Success(3, 3), report.outcome)
    }

    @Test
    fun whatCameBackCountsEvenWhenAnIssuerFailed() {
        val report = SyncReport(
            attempts = listOf(answered(NBP, 1), failed(ECB, requested = 2)),
            firstFailure = SERVER_ERROR,
        )

        assertEquals(SyncOutcome.Success(1, 3), report.outcome)
    }

    @Test
    fun aRunThatFetchedNothingIsReportedForRetry() {
        val report = SyncReport(
            attempts = listOf(failed(NBP, requested = 1), failed(ECB, requested = 2)),
            firstFailure = SERVER_ERROR,
        )

        assertEquals(SERVER_ERROR, report.outcome)
    }

    @Test
    fun anIssuerThatAnsweredWithNothingIsNotAFailure() {
        val report = SyncReport(listOf(answered(NBP, 1), empty(ECB, requested = 2)))

        assertEquals(SyncOutcome.Success(1, 3), report.outcome)
    }

    @Test
    fun aRunWhereEveryIssuerWasSilentSucceedsWithNothing() {
        val report = SyncReport(listOf(empty(NBP, requested = 1), empty(ECB, requested = 2)))

        assertEquals(SyncOutcome.Success(0, 3), report.outcome)
    }

    @Test
    fun anAttemptKnowsWhichOfTheThreeThingsHappened() {
        assertEquals(AttemptResult.Answered, answered(NBP, 1).result)
        assertEquals(AttemptResult.Empty, empty(NBP, requested = 1).result)
        assertEquals(AttemptResult.Failed, failed(NBP, requested = 1).result)
    }

    @Test
    fun aFailedAttemptIsNotMistakenForAnEmptyOne() {
        assertEquals(AttemptResult.Failed, failed(NBP, requested = 2).result)
    }

    @Test
    fun aRunWithNoPairsAtAllAsksForNothing() {
        val report = SyncReport(emptyList())

        assertEquals(SyncOutcome.Success(0, 0), report.outcome)
    }

    private fun answered(provider: ProviderKey, covered: Int) = attempt(
        provider = provider,
        requested = covered,
        covered = covered,
        errorKind = null,
    )

    private fun empty(provider: ProviderKey, requested: Int) = attempt(
        provider = provider,
        requested = requested,
        covered = 0,
        errorKind = null,
    )

    private fun failed(provider: ProviderKey, requested: Int) = attempt(
        provider = provider,
        requested = requested,
        covered = 0,
        errorKind = SyncErrorKind.ServerError,
    )

    private fun attempt(
        provider: ProviderKey,
        requested: Int,
        covered: Int,
        errorKind: SyncErrorKind?,
    ) = ProviderAttempt(
        provider = provider,
        requestedCount = requested,
        coveredCount = covered,
        errorKind = errorKind,
        startedAt = START,
        finishedAt = START,
    )

    private companion object {
        val NBP = ProviderKey("NBP")
        val ECB = ProviderKey("ECB")
        val START = Instant.fromEpochMilliseconds(1_000L)
        val SERVER_ERROR = SyncOutcome.Transient(TransientKind.ServerError)
    }
}
