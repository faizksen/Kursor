package dev.faizksen.kursor.settings.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.faizksen.kursor.settings.navigation.RunDetailRoute
import dev.faizksen.kursor.settings.rundetail.RunDetailViewModel
import dev.faizksen.kursor.settings.syncstatus.SyncStatusViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SyncStatusViewModel)

    viewModel { (handle: SavedStateHandle) ->
        RunDetailViewModel(
            runId = handle.toRoute<RunDetailRoute>().runId,
            observeRun = get(),
            observeRunAttempts = get(),
        )
    }
}
