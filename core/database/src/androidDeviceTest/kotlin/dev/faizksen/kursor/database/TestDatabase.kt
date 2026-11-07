package dev.faizksen.kursor.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

internal fun testDatabase(): KursorDatabase = Room.inMemoryDatabaseBuilder<KursorDatabase>()
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()
