package dev.faizksen.kursor.data.di

import dev.faizksen.kursor.data.AppStateRepository
import dev.faizksen.kursor.data.AppStateRepositoryImpl
import dev.faizksen.kursor.data.CatalogueRepository
import dev.faizksen.kursor.data.CatalogueRepositoryImpl
import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.data.RatesRepositoryImpl
import dev.faizksen.kursor.data.SyncLogRepository
import dev.faizksen.kursor.data.SyncLogRepositoryImpl
import dev.faizksen.kursor.data.WatchedPairsRepository
import dev.faizksen.kursor.data.WatchedPairsRepositoryImpl
import dev.faizksen.kursor.database.di.databaseModule
import dev.faizksen.kursor.network.di.networkModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

val dataModule = module {
    includes(databaseModule, networkModule)

    single<Clock> { Clock.System }

    singleOf(::RatesRepositoryImpl) bind RatesRepository::class
    singleOf(::SyncLogRepositoryImpl) bind SyncLogRepository::class
    singleOf(::CatalogueRepositoryImpl) bind CatalogueRepository::class
    singleOf(::WatchedPairsRepositoryImpl) bind WatchedPairsRepository::class
    singleOf(::AppStateRepositoryImpl) bind AppStateRepository::class
}
