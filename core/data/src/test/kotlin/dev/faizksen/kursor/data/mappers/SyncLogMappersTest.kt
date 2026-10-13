package dev.faizksen.kursor.data.mappers

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
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
import org.junit.Test
import java.time.Instant

class SyncLogMappersTest {

    @Test
    fun runSurvivesRoundTrip() {
        val run = syncRun(
            result = SyncResult.Failed,
            errorKind = SyncErrorKind.RateLimited,
            updatedCount = null,
        )

        assertThat(run.toSyncLogEntity().toSyncRun()).isEqualTo(run)
    }

    @Test
    fun successfulRunSurvivesRoundTrip() {
        val run = syncRun(result = SyncResult.Success, errorKind = null, updatedCount = 12)

        assertThat(run.toSyncLogEntity().toSyncRun()).isEqualTo(run)
    }

    @Test
    fun everySourceSurvivesRoundTrip() {
        SourceId.entries.forEach { source ->
            val run = syncRun(sourceId = source)
            assertWithMessage("source $source")
                .that(run.toSyncLogEntity().toSyncRun())
                .isEqualTo(run)
        }
    }

    @Test
    fun everyTriggerSurvivesRoundTrip() {
        SyncTrigger.entries.forEach { trigger ->
            val run = syncRun(trigger = trigger)
            assertWithMessage("trigger $trigger")
                .that(run.toSyncLogEntity().toSyncRun())
                .isEqualTo(run)
        }
    }

    @Test
    fun everyResultSurvivesRoundTrip() {
        SyncResult.entries.forEach { result ->
            val run = syncRun(result = result)
            assertWithMessage("result $result")
                .that(run.toSyncLogEntity().toSyncRun())
                .isEqualTo(run)
        }
    }

    @Test
    fun everyErrorKindSurvivesRoundTrip() {
        SyncErrorKind.entries.forEach { errorKind ->
            val run = syncRun(errorKind = errorKind)
            assertWithMessage("error kind $errorKind")
                .that(run.toSyncLogEntity().toSyncRun())
                .isEqualTo(run)
        }
    }

    @Test
    fun timestampsAreStoredAsEpochMillis() {
        val run = syncRun()

        val entity = run.toSyncLogEntity()

        assertThat(entity.startedAt).isEqualTo(run.startedAt.toEpochMilli())
        assertThat(entity.finishedAt).isEqualTo(run.finishedAt.toEpochMilli())
    }

    @Test
    fun unrecognizedErrorKindIsReadAsUnknown() {
        val entity = syncLogEntity(errorKind = ErrorColumn.Unrecognized("WhatEver"))

        assertThat(entity.toSyncRun()?.errorKind).isEqualTo(SyncErrorKind.Unknown)
    }

    @Test
    fun rowWithUnrecognizedSourceIsSkipped() {
        val entity = syncLogEntity(sourceId = SourceIdColumn.Unrecognized("BINANCE"))

        assertThat(entity.toSyncRun()).isNull()
    }

    @Test
    fun rowWithUnrecognizedTriggerIsSkipped() {
        val entity = syncLogEntity(trigger = TriggerColumn.Unrecognized("expedited"))

        assertThat(entity.toSyncRun()).isNull()
    }

    @Test
    fun rowWithUnrecognizedResultIsSkipped() {
        val entity = syncLogEntity(result = SyncResultColumn.Unrecognized("cancelled"))

        assertThat(entity.toSyncRun()).isNull()
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
