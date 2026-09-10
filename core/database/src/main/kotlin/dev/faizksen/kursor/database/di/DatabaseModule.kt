package dev.faizksen.kursor.database.di

import android.content.Context
import androidx.room.Room.databaseBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.SeedWatchedPairs
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun kursorDatabase(@ApplicationContext context: Context) = databaseBuilder(
        context = context,
        klass = KursorDatabase::class.java,
        name = "kursor"
    )
        .addCallback(SeedWatchedPairs(nowMillis = System::currentTimeMillis))
        .build()

    @Provides
    fun watchedPairsDao(database: KursorDatabase) = database.watchedPairsDao

    @Provides
    fun ratesDao(database: KursorDatabase) = database.ratesDao
}