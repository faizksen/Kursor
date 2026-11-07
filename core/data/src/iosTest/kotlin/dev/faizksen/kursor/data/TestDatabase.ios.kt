package dev.faizksen.kursor.data

import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.faizksen.kursor.database.KursorDatabase

internal actual fun inMemoryBuilder(): RoomDatabase.Builder<KursorDatabase> =
    Room.inMemoryDatabaseBuilder<KursorDatabase>()
