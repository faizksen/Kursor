package dev.faizksen.kursor.data

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.faizksen.kursor.database.KursorDatabase
import kotlinx.coroutines.Dispatchers

internal expect fun inMemoryBuilder(): RoomDatabase.Builder<KursorDatabase>

internal fun testDatabase(): KursorDatabase = inMemoryBuilder()
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()
