package dev.faizksen.kursor.sync

import androidx.work.Data
import androidx.work.workDataOf
import dev.faizksen.kursor.model.SyncTrigger
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncTriggerDataTest {

    @Test
    fun `every trigger survives a round trip through the input data`() {
        SyncTrigger.entries.forEach { trigger ->
            assertEquals(
                trigger,
                workDataOf(FiatRatesSyncWorker.KEY_TRIGGER to trigger.name).syncTrigger(),
            )
        }
    }

    @Test
    fun `input data without a trigger falls back to the periodic one`() {
        assertEquals(SyncTrigger.Periodic, Data.EMPTY.syncTrigger())
    }

    @Test
    fun `a trigger name the app does not know falls back to the periodic one`() {
        assertEquals(
            SyncTrigger.Periodic,
            workDataOf(FiatRatesSyncWorker.KEY_TRIGGER to "Hourly").syncTrigger(),
        )
    }

    @Test
    fun `a trigger stored under another key is ignored`() {
        assertEquals(SyncTrigger.Periodic, workDataOf("trigger" to "Manual").syncTrigger())
    }
}
