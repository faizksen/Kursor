package dev.faizksen.kursor.database.di

import androidx.room3.Room
import dev.faizksen.kursor.common.di.IoDispatcher
import dev.faizksen.kursor.database.KursorDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformDatabaseModule: Module = module {
    single {
        kursorDatabase(
            builder = Room.databaseBuilder<KursorDatabase>(name = databasePath()),
            clock = get(),
            queryContext = get<IoDispatcher>().dispatcher,
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun databasePath(): String {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )

    return requireNotNull(documents?.path) + "/$DATABASE_NAME"
}
