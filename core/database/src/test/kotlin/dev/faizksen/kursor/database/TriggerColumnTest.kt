package dev.faizksen.kursor.database

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.database.consts.TriggerColumn
import org.junit.Test

class TriggerColumnTest {
    private val syncConverters = SyncLogConverters()

    @Test
    fun `sync converter knows periodic trigger`() {
        val source = TriggerColumn.Periodic
        val restored = syncConverters.toTrigger(syncConverters.fromTrigger(source))
        assertThat(restored).isEqualTo(source)
    }

    @Test
    fun `sync converter knows manual trigger`() {
        val source = TriggerColumn.Manual
        val restored = syncConverters.toTrigger(syncConverters.fromTrigger(source))
        assertThat(restored).isEqualTo(source)
    }

    @Test
    fun `Periodic columnValue is equals with database const`() {
        assertThat(syncConverters.fromTrigger(TriggerColumn.Periodic)).isEqualTo("periodic")
    }

    @Test
    fun `Manual columnValue is equals with database const`() {
        assertThat(syncConverters.fromTrigger(TriggerColumn.Manual)).isEqualTo("manual")
    }

    @Test
    fun `Unrecognized value is acceptable`() {
        assertThat(syncConverters.toTrigger("whatever")).isEqualTo(TriggerColumn.Unrecognized("whatever"))
    }

    @Test
    fun `Unrecognized value stays the same after rewriting to database`() {
        assertThat(syncConverters.fromTrigger(TriggerColumn.Unrecognized("whatever"))).isEqualTo("whatever")
    }

    @Test
    fun `wrong register value in database is Unrecognized`() {
        assertThat(syncConverters.toTrigger("PERIODIC")).isEqualTo(TriggerColumn.Unrecognized("PERIODIC"))
    }
}