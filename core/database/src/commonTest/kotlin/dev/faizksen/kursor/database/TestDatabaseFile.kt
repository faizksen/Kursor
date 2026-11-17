package dev.faizksen.kursor.database

import androidx.room3.RoomDatabase

internal expect fun temporaryDatabasePath(name: String): String

internal expect fun deleteDatabaseFiles(path: String)

internal expect fun fileBuilder(path: String): RoomDatabase.Builder<KursorDatabase>
