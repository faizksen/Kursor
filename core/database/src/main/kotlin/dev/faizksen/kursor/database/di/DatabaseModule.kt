package dev.faizksen.kursor.database.di

import android.content.Context
import androidx.room.Room.databaseBuilder
import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.SeedWatchedPairs
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {
    singleOf(::kursorDatabase)

    factoryOf(::watchedPairsDao)
    factoryOf(::ratesDao)
    factoryOf(::syncLogDao)
}

internal fun kursorDatabase(context: Context): KursorDatabase = databaseBuilder(
    context = context,
    klass = KursorDatabase::class.java,
    name = DATABASE_NAME,
)
    .addCallback(SeedWatchedPairs(nowMillis = System::currentTimeMillis))
    .build()

internal fun watchedPairsDao(database: KursorDatabase): WatchedPairsDao = database.watchedPairsDao

internal fun ratesDao(database: KursorDatabase): RatesDao = database.ratesDao

internal fun syncLogDao(database: KursorDatabase): SyncLogDao = database.syncLogDao

private const val DATABASE_NAME = "kursor"
