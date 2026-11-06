package dev.faizksen.kursor.database.di

import androidx.room.Room.databaseBuilder
import dev.faizksen.kursor.database.KursorDatabase
import dev.faizksen.kursor.database.SeedWatchedPairs
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        databaseBuilder(
            context = androidContext(),
            klass = KursorDatabase::class.java,
            name = "kursor"
        )
            .addCallback(SeedWatchedPairs(nowMillis = System::currentTimeMillis))
            .build()
    }

    factory { get<KursorDatabase>().watchedPairsDao }
    factory { get<KursorDatabase>().ratesDao }
    factory { get<KursorDatabase>().syncLogDao }
}
