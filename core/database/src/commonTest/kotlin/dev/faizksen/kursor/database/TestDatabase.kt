package dev.faizksen.kursor.database

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

internal expect fun inMemoryBuilder(): RoomDatabase.Builder<KursorDatabase>

internal fun testDatabase(): KursorDatabase = inMemoryBuilder()
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()
