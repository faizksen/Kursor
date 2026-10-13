package dev.faizksen.kursor.database

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.database.consts.SourceIdColumn
import org.junit.Test

class SourceIdColumnTest {
    private val converter = RateConverters()

    @Test
    fun `rates converter knows Frankfurter source`() {
        val source = SourceIdColumn.Frankfurter
        val restored = converter.toSourceIdColumn(converter.fromSourceId(source))
        assertThat(restored).isEqualTo(source)
    }

    @Test
    fun `rates converter knows CoinGecko source`() {
        val source = SourceIdColumn.CoinGecko
        val restored = converter.toSourceIdColumn(converter.fromSourceId(source))
        assertThat(restored).isEqualTo(source)
    }

    @Test
    fun `rates converter knows CBR source`() {
        val source = SourceIdColumn.CBR
        val restored = converter.toSourceIdColumn(converter.fromSourceId(source))
        assertThat(restored).isEqualTo(source)
    }

    @Test
    fun `Frankfurter columnValue is equals with database const`() {
        assertThat(converter.fromSourceId(SourceIdColumn.Frankfurter)).isEqualTo("FRANKFURTER")
    }

    @Test
    fun `CoinGecko columnValue is equals with database const`() {
        assertThat(converter.fromSourceId(SourceIdColumn.CoinGecko)).isEqualTo("COIN_GECKO")
    }

    @Test
    fun `CBR columnValue is equals with database const`() {
        assertThat(converter.fromSourceId(SourceIdColumn.CBR)).isEqualTo("CBR")
    }

    @Test
    fun `Unrecognized value is acceptable`() {
        assertThat(converter.toSourceIdColumn("whatever")).isEqualTo(SourceIdColumn.Unrecognized("whatever"))
    }

    @Test
    fun `Unrecognized value stays the same after rewriting to database`() {
        assertThat(converter.fromSourceId(SourceIdColumn.Unrecognized("whatever"))).isEqualTo("whatever")
    }

    @Test
    fun `wrong register value in database is Unrecognized`() {
        assertThat(converter.toSourceIdColumn("Frankfurter")).isEqualTo(SourceIdColumn.Unrecognized("Frankfurter"))
    }
}