package dev.faizksen.kursor.database

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import dev.faizksen.kursor.database.consts.ErrorColumn
import org.junit.Test

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
            assertWithMessage("errorKind $kind").that(restored).isEqualTo(kind)
        }
    }

    @Test
    fun `errors keep their column values`() {
        errorKinds.forEach { (kind, value) ->
            assertWithMessage("errorKind $kind")
                .that(syncConverters.fromErrorColumn(kind))
                .isEqualTo(value)
        }
    }

    @Test
    fun `error kinds have distinct column values`() {
        assertThat(errorKinds.map { (_, value) -> value }).containsNoDuplicates()
    }

    @Test
    fun `Unrecognized value is acceptable`() {
        assertThat(syncConverters.toErrorColumn("whatever")).isEqualTo(ErrorColumn.Unrecognized("whatever"))
    }

    @Test
    fun `Unrecognized value stays the same after rewriting to database`() {
        assertThat(syncConverters.fromErrorColumn(ErrorColumn.Unrecognized("whatever"))).isEqualTo("whatever")
    }

    @Test
    fun `wrong register value in database is Unrecognized`() {
        assertThat(syncConverters.toErrorColumn("nonetwork")).isEqualTo(ErrorColumn.Unrecognized("nonetwork"))
    }
}
