package dev.faizksen.kursor.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

internal class SeedWatchedPairs(
    private val nowMillis: () -> Long,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        val addedAt = nowMillis()
        DEFAULT_PAIRS.forEachIndexed { position, (base, quote) ->
            db.execSQL(
                "INSERT INTO watched_pair (base, quote, position, addedAt) VALUES (?, ?, ?, ?)",
                arrayOf<Any>(base, quote, position, addedAt),
            )
        }
    }

    private companion object {
        val DEFAULT_PAIRS = listOf(
            "EUR" to "PLN",
            "EUR" to "USD",
            "EUR" to "RUB",
        )
    }
}
