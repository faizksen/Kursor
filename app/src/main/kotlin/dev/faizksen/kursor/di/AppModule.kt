package dev.faizksen.kursor.di

import dev.faizksen.kursor.domain.di.domainModule
import dev.faizksen.kursor.settings.di.settingsModule
import dev.faizksen.kursor.sync.di.syncModule
import dev.faizksen.kursor.ui.MainViewModel
import dev.faizksen.kursor.watchlist.di.watchlistModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    includes(
        domainModule,
        syncModule,
        watchlistModule,
        settingsModule,
    )

    viewModelOf(::MainViewModel)
}
