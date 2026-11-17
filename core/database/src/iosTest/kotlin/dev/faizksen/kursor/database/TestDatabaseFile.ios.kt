package dev.faizksen.kursor.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

internal actual fun temporaryDatabasePath(name: String): String = NSTemporaryDirectory() + name

@OptIn(ExperimentalForeignApi::class)
internal actual fun deleteDatabaseFiles(path: String) {
    listOf("", "-wal", "-shm").forEach { suffix ->
        NSFileManager.defaultManager.removeItemAtPath(path + suffix, null)
    }
}

internal actual fun fileBuilder(path: String): RoomDatabase.Builder<KursorDatabase> =
    Room.databaseBuilder<KursorDatabase>(name = path)
