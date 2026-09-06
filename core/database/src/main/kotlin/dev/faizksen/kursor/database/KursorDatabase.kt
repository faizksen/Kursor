package dev.faizksen.kursor.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.faizksen.kursor.database.dao.RatesDao
import dev.faizksen.kursor.database.dao.WatchedPairsDao
import dev.faizksen.kursor.database.entities.RateEntity
import dev.faizksen.kursor.database.entities.WatchedPairEntity

@Database(
    entities = [
        RateEntity::class,
        WatchedPairEntity::class
    ],
    version = KursorDatabase.VERSION,
    exportSchema = true
)
abstract class KursorDatabase: RoomDatabase() {
    companion object {
        const val VERSION = 1
    }

    abstract val watchedPairsDao: WatchedPairsDao

    abstract val ratesDao: RatesDao
}