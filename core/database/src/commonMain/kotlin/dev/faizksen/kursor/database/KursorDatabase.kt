package dev.faizksen.kursor.database

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import dev.faizksen.kursor.database.dao.AppStateDao
import dev.faizksen.kursor.database.dao.CatalogueDao
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.SyncAttemptDao
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.AppStateEntity
import dev.faizksen.kursor.database.entities.CatalogueMetaEntity
import dev.faizksen.kursor.database.entities.CurrencyEntity
import dev.faizksen.kursor.database.entities.ProviderCoverageEntity
import dev.faizksen.kursor.database.entities.ProviderEntity
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.database.entities.SyncAttemptEntity
import dev.faizksen.kursor.database.entities.SyncLogEntity
import dev.faizksen.kursor.database.entities.WatchedPairEntity

@Database(
    entities = [
        RateEntity::class,
        WatchedPairEntity::class,
        SyncLogEntity::class,
        CurrencyEntity::class,
        ProviderEntity::class,
        ProviderCoverageEntity::class,
        CatalogueMetaEntity::class,
        AppStateEntity::class,
        SyncAttemptEntity::class
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8)
    ],
    version = KursorDatabase.VERSION,
    exportSchema = true
)
@ColumnTypeConverters(
    SyncLogConverters::class,
    RateConverters::class,
    CatalogueConverters::class
)
@ConstructedBy(KursorDatabaseConstructor::class)
abstract class KursorDatabase: RoomDatabase() {
    companion object {
        const val VERSION = 8
    }

    abstract val watchedPairsDao: WatchedPairsDao

    abstract val ratesDao: RatesDao

    abstract val syncLogDao: SyncLogDao

    abstract val syncAttemptDao: SyncAttemptDao

    abstract val catalogueDao: CatalogueDao

    abstract val appStateDao: AppStateDao
}

@Suppress("KotlinNoActualForExpect")
expect object KursorDatabaseConstructor : RoomDatabaseConstructor<KursorDatabase> {
    override fun initialize(): KursorDatabase
}
