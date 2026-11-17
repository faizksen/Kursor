package dev.faizksen.kursor.watchlist.di

import dev.faizksen.kursor.watchlist.WatchlistViewModel
import dev.faizksen.kursor.watchlist.addpair.AddPairViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val watchlistModule = module {
    viewModelOf(::WatchlistViewModel)
    viewModelOf(::AddPairViewModel)
}
