package dev.faizksen.kursor.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import dev.faizksen.kursor.domain.ObserveSyncLogUseCase
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCase
import dev.faizksen.kursor.domain.ObserveSyncStateUseCase
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.domain.RunSyncUseCase
import dev.faizksen.kursor.domain.SyncScheduler
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
class AppGraphTest {

    private val koin = GlobalContext.get()

    @Test
    fun theGraphBuildsEverySyncDependency() {
        assertNotNull(koin.get<RunSyncUseCase>())
        assertNotNull(koin.get<SyncScheduler>())
        assertNotNull(koin.get<RequestManualSyncUseCase>())
        assertNotNull(koin.get<WorkManager>())
    }

    @Test
    fun theGraphBuildsEveryScreenDependency() {
        assertNotNull(koin.get<ObserveWatchlistUseCase>())
        assertNotNull(koin.get<ObserveSyncLogUseCase>())
        assertNotNull(koin.get<ObserveSyncQueueUseCase>())
        assertNotNull(koin.get<ObserveSyncStateUseCase>())
    }
}
