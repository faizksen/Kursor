package dev.faizksen.kursor.database

import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.SyncLogDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.database.entities.SyncLogEntity
import dev.faizksen.kursor.database.entities.WatchedPairEntity

@Database(
    entities = [
        RateEntity::class,
        WatchedPairEntity::class,
        SyncLogEntity::class
    ],
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    version = KursorDatabase.VERSION,
    exportSchema = true
)
@ColumnTypeConverters(
    SyncLogConverters::class,
    RateConverters::class
)
abstract class KursorDatabase: RoomDatabase() {
    companion object {
        const val VERSION = 2
    }

    abstract val watchedPairsDao: WatchedPairsDao

    abstract val ratesDao: RatesDao

    abstract val syncLogDao: SyncLogDao
}