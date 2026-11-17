package dev.faizksen.kursor.watchlist.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.faizksen.kursor.watchlist.WatchlistViewModel
import dev.faizksen.kursor.watchlist.addpair.AddPairViewModel
import dev.faizksen.kursor.watchlist.changeprovider.ChangeProviderViewModel
import dev.faizksen.kursor.watchlist.navigation.ChangeProviderRoute
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val watchlistModule = module {
    viewModelOf(::WatchlistViewModel)
    viewModelOf(::AddPairViewModel)

    viewModel { (handle: SavedStateHandle) ->
        ChangeProviderViewModel(
            pairId = handle.toRoute<ChangeProviderRoute>().pairId,
            watchedPair = get(),
            providerCandidates = get(),
            changeProvider = get(),
        )
    }
}
