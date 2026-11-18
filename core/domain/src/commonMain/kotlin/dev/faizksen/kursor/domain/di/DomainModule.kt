package dev.faizksen.kursor.domain.di

import dev.faizksen.kursor.data.di.dataModule
import dev.faizksen.kursor.domain.AddWatchedPairUseCase
import dev.faizksen.kursor.domain.AddWatchedPairUseCaseImpl
import dev.faizksen.kursor.domain.ChangeProviderUseCase
import dev.faizksen.kursor.domain.ChangeProviderUseCaseImpl
import dev.faizksen.kursor.domain.ObserveRunAttemptsUseCase
import dev.faizksen.kursor.domain.ObserveRunAttemptsUseCaseImpl
import dev.faizksen.kursor.domain.WatchedPairUseCase
import dev.faizksen.kursor.domain.WatchedPairUseCaseImpl
import dev.faizksen.kursor.domain.DeletePairUseCase
import dev.faizksen.kursor.domain.DeletePairUseCaseImpl
import dev.faizksen.kursor.domain.FlipPairUseCase
import dev.faizksen.kursor.domain.FlipPairUseCaseImpl
import dev.faizksen.kursor.domain.ForgetDeletedPairUseCase
import dev.faizksen.kursor.domain.ForgetDeletedPairUseCaseImpl
import dev.faizksen.kursor.domain.ObserveDeletedPairUseCase
import dev.faizksen.kursor.domain.ObserveDeletedPairUseCaseImpl
import dev.faizksen.kursor.domain.ObservePairDetailUseCase
import dev.faizksen.kursor.domain.ObservePairDetailUseCaseImpl
import dev.faizksen.kursor.domain.ObserveRunUseCase
import dev.faizksen.kursor.domain.ObserveRunUseCaseImpl
import dev.faizksen.kursor.domain.ObserveSyncLogUseCase
import dev.faizksen.kursor.domain.ObserveSyncLogUseCaseImpl
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCase
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCaseImpl
import dev.faizksen.kursor.domain.ObserveSyncStateUseCase
import dev.faizksen.kursor.domain.ObserveSyncStateUseCaseImpl
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.domain.ObserveWatchlistUseCaseImpl
import dev.faizksen.kursor.domain.ProviderCandidatesUseCase
import dev.faizksen.kursor.domain.ProviderCandidatesUseCaseImpl
import dev.faizksen.kursor.domain.PickableCurrenciesUseCase
import dev.faizksen.kursor.domain.PickableCurrenciesUseCaseImpl
import dev.faizksen.kursor.domain.PruneSyncLogUseCase
import dev.faizksen.kursor.domain.PruneSyncLogUseCaseImpl
import dev.faizksen.kursor.domain.RecordSyncRunUseCase
import dev.faizksen.kursor.domain.RefreshCatalogueUseCase
import dev.faizksen.kursor.domain.RefreshCatalogueUseCaseImpl
import dev.faizksen.kursor.domain.ResolveMissingProvidersUseCase
import dev.faizksen.kursor.domain.ResolveMissingProvidersUseCaseImpl
import dev.faizksen.kursor.domain.RefreshRatesUseCase
import dev.faizksen.kursor.domain.ReorderPairsUseCase
import dev.faizksen.kursor.domain.ReorderPairsUseCaseImpl
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCaseImpl
import dev.faizksen.kursor.domain.ObserveDefaultsSeededUseCase
import dev.faizksen.kursor.domain.ObserveDefaultsSeededUseCaseImpl
import dev.faizksen.kursor.domain.RunSyncUseCase
import dev.faizksen.kursor.domain.SeedDefaultPairsUseCase
import dev.faizksen.kursor.domain.SeedDefaultPairsUseCaseImpl
import dev.faizksen.kursor.domain.SyncMissingRatesUseCase
import dev.faizksen.kursor.domain.SyncMissingRatesUseCaseImpl
import dev.faizksen.kursor.domain.UndoDeletedPairUseCase
import dev.faizksen.kursor.domain.UndoDeletedPairUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

    factoryOf(::ObserveWatchlistUseCaseImpl) bind ObserveWatchlistUseCase::class
    factoryOf(::ObserveSyncStateUseCaseImpl) bind ObserveSyncStateUseCase::class
    factoryOf(::ObserveSyncLogUseCaseImpl) bind ObserveSyncLogUseCase::class
    factoryOf(::ObserveSyncQueueUseCaseImpl) bind ObserveSyncQueueUseCase::class
    factoryOf(::ObserveRunUseCaseImpl) bind ObserveRunUseCase::class
    factoryOf(::ObservePairDetailUseCaseImpl) bind ObservePairDetailUseCase::class
    factoryOf(::DeletePairUseCaseImpl) bind DeletePairUseCase::class
    factoryOf(::FlipPairUseCaseImpl) bind FlipPairUseCase::class
    factoryOf(::ReorderPairsUseCaseImpl) bind ReorderPairsUseCase::class
    factoryOf(::UndoDeletedPairUseCaseImpl) bind UndoDeletedPairUseCase::class
    factoryOf(::ForgetDeletedPairUseCaseImpl) bind ForgetDeletedPairUseCase::class
    factoryOf(::ObserveDeletedPairUseCaseImpl) bind ObserveDeletedPairUseCase::class
    factoryOf(::ObserveRunAttemptsUseCaseImpl) bind ObserveRunAttemptsUseCase::class
    factoryOf(::RequestManualSyncUseCaseImpl) bind RequestManualSyncUseCase::class
    factoryOf(::RefreshRatesUseCase)
    factoryOf(::RecordSyncRunUseCase)
    factoryOf(::RunSyncUseCase)
    factoryOf(::PruneSyncLogUseCaseImpl) bind PruneSyncLogUseCase::class
    factoryOf(::ProviderCandidatesUseCaseImpl) bind ProviderCandidatesUseCase::class
    factoryOf(::PickableCurrenciesUseCaseImpl) bind PickableCurrenciesUseCase::class
    factoryOf(::RefreshCatalogueUseCaseImpl) bind RefreshCatalogueUseCase::class
    factoryOf(::AddWatchedPairUseCaseImpl) bind AddWatchedPairUseCase::class
    factoryOf(::WatchedPairUseCaseImpl) bind WatchedPairUseCase::class
    factoryOf(::ChangeProviderUseCaseImpl) bind ChangeProviderUseCase::class
    factoryOf(::SyncMissingRatesUseCaseImpl) bind SyncMissingRatesUseCase::class
    factoryOf(::SeedDefaultPairsUseCaseImpl) bind SeedDefaultPairsUseCase::class
    factoryOf(::ObserveDefaultsSeededUseCaseImpl) bind ObserveDefaultsSeededUseCase::class
    factoryOf(::ResolveMissingProvidersUseCaseImpl) bind ResolveMissingProvidersUseCase::class
}
