package dev.faizksen.kursor.data.mappers

import dev.faizksen.kursor.database.consts.ErrorColumn
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.consts.SyncResultColumn
import dev.faizksen.kursor.database.consts.TriggerColumn
import dev.faizksen.kursor.database.entities.SyncLogEntity
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncErrorKind
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncTrigger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SyncLogMappersTest {

    @Test
    fun runSurvivesRoundTrip() {
        val run = syncRun(
            result = SyncResult.Failed,
            errorKind = SyncErrorKind.RateLimited,
            updatedCount = null,
        )

        assertEquals(run, run.toSyncLogEntity().toSyncRun())
    }

    @Test
    fun successfulRunSurvivesRoundTrip() {
        val run = syncRun(result = SyncResult.Success, errorKind = null, updatedCount = 12)

        assertEquals(run, run.toSyncLogEntity().toSyncRun())
    }

    @Test
    fun everySourceSurvivesRoundTrip() {
        SourceId.entries.forEach { source ->
            val run = syncRun(sourceId = source)
            assertEquals(run, run.toSyncLogEntity().toSyncRun(), "source $source")
        }
    }

    @Test
    fun everyTriggerSurvivesRoundTrip() {
        SyncTrigger.entries.forEach { trigger ->
            val run = syncRun(trigger = trigger)
            assertEquals(run, run.toSyncLogEntity().toSyncRun(), "trigger $trigger")
        }
    }

    @Test
    fun everyResultSurvivesRoundTrip() {
        SyncResult.entries.forEach { result ->
            val run = syncRun(result = result)
            assertEquals(run, run.toSyncLogEntity().toSyncRun(), "result $result")
        }
    }

    @Test
    fun everyErrorKindSurvivesRoundTrip() {
        SyncErrorKind.entries.forEach { errorKind ->
            val run = syncRun(errorKind = errorKind)
            assertEquals(run, run.toSyncLogEntity().toSyncRun(), "error kind $errorKind")
        }
    }

    @Test
    fun timestampsAreStoredAsEpochMillis() {
        val run = syncRun()

        val entity = run.toSyncLogEntity()

        assertEquals(run.startedAt.toEpochMilliseconds(), entity.startedAt)
        assertEquals(run.finishedAt.toEpochMilliseconds(), entity.finishedAt)
    }

    @Test
    fun unrecognizedErrorKindIsReadAsUnknown() {
        val entity = syncLogEntity(errorKind = ErrorColumn.Unrecognized("WhatEver"))

        assertEquals(SyncErrorKind.Unknown, entity.toSyncRun()?.errorKind)
    }

    @Test
    fun rowWithUnrecognizedSourceIsSkipped() {
        val entity = syncLogEntity(sourceId = SourceIdColumn.Unrecognized("BINANCE"))

        assertNull(entity.toSyncRun())
    }

    @Test
    fun rowWithUnrecognizedTriggerIsSkipped() {
        val entity = syncLogEntity(trigger = TriggerColumn.Unrecognized("expedited"))

        assertNull(entity.toSyncRun())
    }

    @Test
    fun rowWithUnrecognizedResultIsSkipped() {
        val entity = syncLogEntity(result = SyncResultColumn.Unrecognized("cancelled"))

        assertNull(entity.toSyncRun())
    }

    private fun syncRun(
        sourceId: SourceId = SourceId.FRANKFURTER,
        trigger: SyncTrigger = SyncTrigger.Periodic,
        result: SyncResult = SyncResult.Success,
        errorKind: SyncErrorKind? = null,
        updatedCount: Int? = 3,
    ) = SyncRun(
        id = 42,
        sourceId = sourceId,
        trigger = trigger,
        startedAt = Instant.parse("2026-10-13T10:00:00Z"),
        finishedAt = Instant.parse("2026-10-13T10:00:02Z"),
        result = result,
        errorKind = errorKind,
        updatedCount = updatedCount,
        attempt = 1,
    )

    private fun syncLogEntity(
        sourceId: SourceIdColumn = SourceIdColumn.Frankfurter,
        trigger: TriggerColumn = TriggerColumn.Periodic,
        result: SyncResultColumn = SyncResultColumn.Failed,
        errorKind: ErrorColumn? = ErrorColumn.Timeout,
    ) = SyncLogEntity(
        id = 42,
        sourceId = sourceId,
        trigger = trigger,
        startedAt = 1_000L,
        finishedAt = 3_000L,
        result = result,
        errorKind = errorKind,
        updatedCount = null,
        attempt = 1,
    )
}
