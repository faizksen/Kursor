package dev.faizksen.kursor.database

import dev.faizksen.kursor.database.consts.SourceIdColumn
import kotlin.test.Test
import kotlin.test.assertEquals

class SourceIdColumnTest {
    private val converter = RateConverters()

    @Test
    fun `rates converter knows Frankfurter source`() {
        val source = SourceIdColumn.Frankfurter
        val restored = converter.toSourceIdColumn(converter.fromSourceId(source))
        assertEquals(source, restored)
    }

    @Test
    fun `rates converter knows CoinGecko source`() {
        val source = SourceIdColumn.CoinGecko
        val restored = converter.toSourceIdColumn(converter.fromSourceId(source))
        assertEquals(source, restored)
    }

    @Test
    fun `rates converter knows CBR source`() {
        val source = SourceIdColumn.CBR
        val restored = converter.toSourceIdColumn(converter.fromSourceId(source))
        assertEquals(source, restored)
    }

    @Test
    fun `Frankfurter columnValue is equals with database const`() {
        assertEquals("FRANKFURTER", converter.fromSourceId(SourceIdColumn.Frankfurter))
    }

    @Test
    fun `CoinGecko columnValue is equals with database const`() {
        assertEquals("COIN_GECKO", converter.fromSourceId(SourceIdColumn.CoinGecko))
    }

    @Test
    fun `CBR columnValue is equals with database const`() {
        assertEquals("CBR", converter.fromSourceId(SourceIdColumn.CBR))
    }

    @Test
    fun `Unrecognized value is acceptable`() {
        assertEquals(SourceIdColumn.Unrecognized("whatever"), converter.toSourceIdColumn("whatever"))
    }

    @Test
    fun `Unrecognized value stays the same after rewriting to database`() {
        assertEquals("whatever", converter.fromSourceId(SourceIdColumn.Unrecognized("whatever")))
    }

    @Test
    fun `wrong register value in database is Unrecognized`() {
        assertEquals(SourceIdColumn.Unrecognized("Frankfurter"), converter.toSourceIdColumn("Frankfurter"))
    }
}