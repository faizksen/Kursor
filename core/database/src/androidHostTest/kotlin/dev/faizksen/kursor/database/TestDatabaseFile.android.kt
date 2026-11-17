package dev.faizksen.kursor.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

internal actual fun temporaryDatabasePath(name: String): String =
    File(System.getProperty("java.io.tmpdir"), name).absolutePath

internal actual fun deleteDatabaseFiles(path: String) {
    listOf("", "-wal", "-shm").forEach { suffix -> File(path + suffix).delete() }
}

internal actual fun fileBuilder(path: String): RoomDatabase.Builder<KursorDatabase> =
    Room.databaseBuilder<KursorDatabase>(name = path)
