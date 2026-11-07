package dev.faizksen.kursor.database

import dev.faizksen.kursor.database.consts.SyncResultColumn
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncResultColumnTest {
    private val syncConverters = SyncLogConverters()

    @Test
    fun `sync converter knows success SyncResult`() {
        val source = SyncResultColumn.Success
        val restored = syncConverters.toResult(syncConverters.fromResult(source))
        assertEquals(source, restored)
    }

    @Test
    fun `sync converter knows retry SyncResult`() {
        val source = SyncResultColumn.Retry
        val restored = syncConverters.toResult(syncConverters.fromResult(source))
        assertEquals(source, restored)
    }

    @Test
    fun `sync converter knows failed SyncResult`() {
        val source = SyncResultColumn.Failed
        val restored = syncConverters.toResult(syncConverters.fromResult(source))
        assertEquals(source, restored)
    }

    @Test
    fun `Success columnValue is equals with database const`() {
        assertEquals("success", syncConverters.fromResult(SyncResultColumn.Success))
    }

    @Test
    fun `Retry columnValue is equals with database const`() {
        assertEquals("retry", syncConverters.fromResult(SyncResultColumn.Retry))
    }

    @Test
    fun `Failed columnValue is equals with database const`() {
        assertEquals("failed", syncConverters.fromResult(SyncResultColumn.Failed))
    }

    @Test
    fun `Unrecognized value is acceptable`() {
        assertEquals(SyncResultColumn.Unrecognized("whatever"), syncConverters.toResult("whatever"))
    }

    @Test
    fun `Unrecognized value stays the same after rewriting to database`() {
        assertEquals("whatever", syncConverters.fromResult(SyncResultColumn.Unrecognized("whatever")))
    }

    @Test
    fun `wrong register value in database is Unrecognized`() {
        assertEquals(SyncResultColumn.Unrecognized("SUCCESS"), syncConverters.toResult("SUCCESS"))
    }
}