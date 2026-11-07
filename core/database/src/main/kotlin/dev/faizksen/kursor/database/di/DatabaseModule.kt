package dev.faizksen.kursor.database.di

import android.content.Context
import androidx.room3.Room.databaseBuilder
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.SeedWatchedPairs
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Clock

val databaseModule = module {
    singleOf(::kursorDatabase)

    factoryOf(::watchedPairsDao)
    factoryOf(::ratesDao)
    factoryOf(::syncLogDao)
}

internal fun kursorDatabase(context: Context, clock: Clock): KursorDatabase = databaseBuilder(
    context = context,
    klass = KursorDatabase::class.java,
    name = DATABASE_NAME,
)
    .addCallback(SeedWatchedPairs(clock))
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

internal fun watchedPairsDao(database: KursorDatabase): WatchedPairsDao = database.watchedPairsDao

internal fun ratesDao(database: KursorDatabase): RatesDao = database.ratesDao

internal fun syncLogDao(database: KursorDatabase): SyncLogDao = database.syncLogDao

private const val DATABASE_NAME = "kursor"
