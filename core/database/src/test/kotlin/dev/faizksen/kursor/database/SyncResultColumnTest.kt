package dev.faizksen.kursor.database

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.database.consts.SyncResultColumn
import org.junit.Test

class SyncResultColumnTest {
    private val syncConverters = SyncLogConverters()

    @Test
    fun `sync converter knows success SyncResult`() {
        val source = SyncResultColumn.Success
        val restored = syncConverters.toResult(syncConverters.fromResult(source))
        assertThat(restored).isEqualTo(source)
    }

    @Test
    fun `sync converter knows retry SyncResult`() {
        val source = SyncResultColumn.Retry
        val restored = syncConverters.toResult(syncConverters.fromResult(source))
        assertThat(restored).isEqualTo(source)
    }

    @Test
    fun `sync converter knows failed SyncResult`() {
        val source = SyncResultColumn.Failed
        val restored = syncConverters.toResult(syncConverters.fromResult(source))
        assertThat(restored).isEqualTo(source)
    }

    @Test
    fun `Success columnValue is equals with database const`() {
        assertThat(syncConverters.fromResult(SyncResultColumn.Success)).isEqualTo("success")
    }

    @Test
    fun `Retry columnValue is equals with database const`() {
        assertThat(syncConverters.fromResult(SyncResultColumn.Retry)).isEqualTo("retry")
    }

    @Test
    fun `Failed columnValue is equals with database const`() {
        assertThat(syncConverters.fromResult(SyncResultColumn.Failed)).isEqualTo("failed")
    }

    @Test
    fun `Unrecognized value is acceptable`() {
        assertThat(syncConverters.toResult("whatever")).isEqualTo(SyncResultColumn.Unrecognized("whatever"))
    }

    @Test
    fun `Unrecognized value stays the same after rewriting to database`() {
        assertThat(syncConverters.fromResult(SyncResultColumn.Unrecognized("whatever"))).isEqualTo("whatever")
    }

    @Test
    fun `wrong register value in database is Unrecognized`() {
        assertThat(syncConverters.toResult("SUCCESS")).isEqualTo(SyncResultColumn.Unrecognized("SUCCESS"))
    }
}