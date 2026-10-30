package dev.faizksen.kursor.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.faizksen.kursor.domain.ObserveSyncLogUseCase
import dev.faizksen.kursor.domain.ObserveSyncLogUseCaseImpl
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCase
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCaseImpl
import dev.faizksen.kursor.domain.ObserveSyncStateUseCase
import dev.faizksen.kursor.domain.ObserveSyncStateUseCaseImpl
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.domain.ObserveWatchlistUseCaseImpl
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCaseImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface DomainModule {

    @Binds
    fun bindObserveWatchlistUseCase(impl: ObserveWatchlistUseCaseImpl): ObserveWatchlistUseCase

    @Binds
    fun bindObserveSyncStateUseCase(impl: ObserveSyncStateUseCaseImpl): ObserveSyncStateUseCase

    @Binds
    fun bindObserveSyncLogUseCase(impl: ObserveSyncLogUseCaseImpl): ObserveSyncLogUseCase

    @Binds
    fun bindRequestManualSyncUseCase(impl: RequestManualSyncUseCaseImpl): RequestManualSyncUseCase

    @Binds
    fun bindObserveSyncQueueUseCase(impl: ObserveSyncQueueUseCaseImpl): ObserveSyncQueueUseCase
}