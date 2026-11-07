package dev.faizksen.kursor.database.di

import android.content.Context
import androidx.room3.Room
import dev.faizksen.kursor.common.di.IoDispatcher
import dev.faizksen.kursor.database.KursorDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    single {
        val context = get<Context>()

        kursorDatabase(
            builder = Room.databaseBuilder<KursorDatabase>(
                context = context,
                name = context.getDatabasePath(DATABASE_NAME).absolutePath,
            ),
            clock = get(),
            queryContext = get<IoDispatcher>().dispatcher,
        )
    }
}
