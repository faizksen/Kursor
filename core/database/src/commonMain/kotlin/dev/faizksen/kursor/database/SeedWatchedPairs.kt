package dev.faizksen.kursor.database

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import kotlin.time.Clock
import kotlin.use

internal class SeedWatchedPairs(
    private val clock: Clock,
) : RoomDatabase.Callback() {

    override suspend fun onCreate(connection: SQLiteConnection) {
        val addedAt = clock.now().toEpochMilliseconds()

        DEFAULT_PAIRS.forEachIndexed { position, (base, quote) ->
            connection.prepare(INSERT_PAIR).use { statement ->
                statement.bindText(1, base)
                statement.bindText(2, quote)
                statement.bindInt(3, position)
                statement.bindLong(4, addedAt)
                statement.step()
            }
        }
    }

    private companion object {
        const val INSERT_PAIR =
            "INSERT INTO watched_pair (base, quote, position, addedAt) VALUES (?, ?, ?, ?)"

        val DEFAULT_PAIRS = listOf(
            "EUR" to "PLN",
            "EUR" to "USD",
            "EUR" to "RUB",
        )
    }
}
