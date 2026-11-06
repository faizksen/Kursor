package dev.faizksen.kursor.settings.di

import dev.faizksen.kursor.settings.syncstatus.SyncStatusViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SyncStatusViewModel)
}
