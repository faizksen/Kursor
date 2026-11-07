package dev.faizksen.kursor.domain.di

import dev.faizksen.kursor.data.di.dataModule
import dev.faizksen.kursor.domain.ObserveSyncLogUseCase
import dev.faizksen.kursor.domain.ObserveSyncLogUseCaseImpl
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCase
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCaseImpl
import dev.faizksen.kursor.domain.ObserveSyncStateUseCase
import dev.faizksen.kursor.domain.ObserveSyncStateUseCaseImpl
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.domain.ObserveWatchlistUseCaseImpl
import dev.faizksen.kursor.domain.RecordSyncRunUseCase
import dev.faizksen.kursor.domain.RefreshRatesUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCaseImpl
import dev.faizksen.kursor.domain.RunSyncUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

    factoryOf(::ObserveWatchlistUseCaseImpl) bind ObserveWatchlistUseCase::class
    factoryOf(::ObserveSyncStateUseCaseImpl) bind ObserveSyncStateUseCase::class
    factoryOf(::ObserveSyncLogUseCaseImpl) bind ObserveSyncLogUseCase::class
    factoryOf(::ObserveSyncQueueUseCaseImpl) bind ObserveSyncQueueUseCase::class
    factoryOf(::RequestManualSyncUseCaseImpl) bind RequestManualSyncUseCase::class
    factoryOf(::RefreshRatesUseCase)
    factoryOf(::RecordSyncRunUseCase)
    factoryOf(::RunSyncUseCase)
}
