package dev.faizksen.kursor.database

import dev.faizksen.kursor.database.consts.ErrorColumn
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorColumnTest {
    private val syncConverters = SyncLogConverters()

    private val errorKinds = listOf(
        ErrorColumn.NoNetwork to "NoNetwork",
        ErrorColumn.Timeout to "Timeout",
        ErrorColumn.ServerError to "ServerError",
        ErrorColumn.RateLimited to "RateLimited",
        ErrorColumn.InvalidCurrency to "InvalidCurrency",
        ErrorColumn.ParseError to "ParseError",
        ErrorColumn.UnknownSource to "UnknownSource",
        ErrorColumn.Unknown to "Unknown",
    )

    @Test
    fun `sync converter knows all of error kind`() {
        errorKinds.forEach { (kind, _) ->
            val restored = syncConverters.toErrorColumn(syncConverters.fromErrorColumn(kind))
            assertEquals(kind, restored, "errorKind $kind")
        }
    }

    @Test
    fun `errors keep their column values`() {
        errorKinds.forEach { (kind, value) ->
            assertEquals(value, syncConverters.fromErrorColumn(kind), "errorKind $kind")
        }
    }

    @Test
    fun `error kinds have distinct column values`() {
        val values = errorKinds.map { (_, value) -> value }
        assertEquals(values.size, values.toSet().size)
    }

    @Test
    fun `Unrecognized value is acceptable`() {
        assertEquals(ErrorColumn.Unrecognized("whatever"), syncConverters.toErrorColumn("whatever"))
    }

    @Test
    fun `Unrecognized value stays the same after rewriting to database`() {
        assertEquals("whatever", syncConverters.fromErrorColumn(ErrorColumn.Unrecognized("whatever")))
    }

    @Test
    fun `wrong register value in database is Unrecognized`() {
        assertEquals(ErrorColumn.Unrecognized("nonetwork"), syncConverters.toErrorColumn("nonetwork"))
    }
}
