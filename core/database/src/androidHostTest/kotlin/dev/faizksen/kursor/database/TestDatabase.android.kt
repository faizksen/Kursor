package dev.faizksen.kursor.database

import androidx.room3.Room
import androidx.room3.RoomDatabase

internal actual fun inMemoryBuilder(): RoomDatabase.Builder<KursorDatabase> =
    Room.inMemoryDatabaseBuilder<KursorDatabase>()
