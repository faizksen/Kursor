package dev.faizksen.kursor.sync.di

import androidx.work.WorkManager
import dev.faizksen.kursor.domain.SyncScheduler
import dev.faizksen.kursor.sync.FiatRatesSyncWorker
import dev.faizksen.kursor.sync.SyncLogger
import dev.faizksen.kursor.sync.SyncSchedulerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val syncModule = module {
    single { WorkManager.getInstance(androidContext()) }

    singleOf(::SyncSchedulerImpl) bind SyncScheduler::class
    singleOf(::SyncLogger)

    workerOf(::FiatRatesSyncWorker)
}
