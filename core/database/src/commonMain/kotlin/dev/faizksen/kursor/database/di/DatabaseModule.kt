package dev.faizksen.kursor.database.di

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.faizksen.kursor.common.di.dispatchersModule
import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.MigrateSeedingOutOfTheDatabase
import dev.faizksen.kursor.database.MigrateToProviderPerPair
import dev.faizksen.kursor.database.dao.AppStateDao
import dev.faizksen.kursor.database.dao.CatalogueDao
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.SyncAttemptDao
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock

expect val platformDatabaseModule: Module

val databaseModule = module {
    includes(dispatchersModule, platformDatabaseModule)

    factoryOf(::watchedPairsDao)
    factoryOf(::ratesDao)
    factoryOf(::syncLogDao)
    factoryOf(::catalogueDao)
    factoryOf(::appStateDao)
    factoryOf(::syncAttemptDao)
}

internal fun kursorDatabase(
    builder: RoomDatabase.Builder<KursorDatabase>,
    clock: Clock,
    queryContext: CoroutineContext,
): KursorDatabase = builder
    .addMigrations(MigrateToProviderPerPair, MigrateSeedingOutOfTheDatabase)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(queryContext)
    .build()

internal fun watchedPairsDao(database: KursorDatabase): WatchedPairsDao = database.watchedPairsDao

internal fun ratesDao(database: KursorDatabase): RatesDao = database.ratesDao

internal fun syncLogDao(database: KursorDatabase): SyncLogDao = database.syncLogDao

internal fun catalogueDao(database: KursorDatabase): CatalogueDao = database.catalogueDao

internal fun appStateDao(database: KursorDatabase): AppStateDao = database.appStateDao

internal fun syncAttemptDao(database: KursorDatabase): SyncAttemptDao = database.syncAttemptDao

internal const val DATABASE_NAME = "kursor"
