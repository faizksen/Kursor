package dev.faizksen.kursor.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
@TypeConverters(
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