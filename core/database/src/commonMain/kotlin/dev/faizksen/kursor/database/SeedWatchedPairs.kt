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

        DEFAULT_WATCHED_PAIRS.forEachIndexed { position, pair ->
            connection.prepare(INSERT_PAIR).use { statement ->
                statement.bindText(1, pair.base)
                statement.bindText(2, pair.quote)
                statement.bindText(3, pair.provider)
                statement.bindInt(4, position)
                statement.bindLong(5, addedAt)
                statement.step()
            }
        }
    }

    private companion object {
        const val INSERT_PAIR =
            "INSERT INTO watched_pair (base, quote, provider, position, addedAt) " +
                "VALUES (?, ?, ?, ?, ?)"
    }
}
