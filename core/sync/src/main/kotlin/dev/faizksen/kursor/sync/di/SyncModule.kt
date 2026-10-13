package dev.faizksen.kursor.sync.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.faizksen.kursor.domain.SyncScheduler
import dev.faizksen.kursor.sync.SyncSchedulerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface SyncBindsModule {
    @Binds
    @Singleton
    fun bindSyncScheduler(impl: SyncSchedulerImpl): SyncScheduler
}

@Module
@InstallIn(SingletonComponent::class)
internal object SyncProvidesModule {
    @Provides
    @Singleton
    fun workManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}