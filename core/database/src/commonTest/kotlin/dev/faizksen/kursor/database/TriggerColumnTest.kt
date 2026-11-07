package dev.faizksen.kursor.database

import dev.faizksen.kursor.database.consts.TriggerColumn
import kotlin.test.Test
import kotlin.test.assertEquals

class TriggerColumnTest {
    private val syncConverters = SyncLogConverters()

    @Test
    fun `sync converter knows periodic trigger`() {
        val source = TriggerColumn.Periodic
        val restored = syncConverters.toTrigger(syncConverters.fromTrigger(source))
        assertEquals(source, restored)
    }

    @Test
    fun `sync converter knows manual trigger`() {
        val source = TriggerColumn.Manual
        val restored = syncConverters.toTrigger(syncConverters.fromTrigger(source))
        assertEquals(source, restored)
    }

    @Test
    fun `Periodic columnValue is equals with database const`() {
        assertEquals("periodic", syncConverters.fromTrigger(TriggerColumn.Periodic))
    }

    @Test
    fun `Manual columnValue is equals with database const`() {
        assertEquals("manual", syncConverters.fromTrigger(TriggerColumn.Manual))
    }

    @Test
    fun `Unrecognized value is acceptable`() {
        assertEquals(TriggerColumn.Unrecognized("whatever"), syncConverters.toTrigger("whatever"))
    }

    @Test
    fun `Unrecognized value stays the same after rewriting to database`() {
        assertEquals("whatever", syncConverters.fromTrigger(TriggerColumn.Unrecognized("whatever")))
    }

    @Test
    fun `wrong register value in database is Unrecognized`() {
        assertEquals(TriggerColumn.Unrecognized("PERIODIC"), syncConverters.toTrigger("PERIODIC"))
    }
}