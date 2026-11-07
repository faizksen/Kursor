package dev.faizksen.kursor.sync.di

import android.content.Context
import androidx.work.WorkManager
import dev.faizksen.kursor.domain.SyncScheduler
import dev.faizksen.kursor.sync.FiatRatesSyncWorker
import dev.faizksen.kursor.sync.SyncSchedulerImpl
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val syncModule = module {
    singleOf<WorkManager, Context>(WorkManager::getInstance)

    singleOf(::SyncSchedulerImpl) bind SyncScheduler::class

    workerOf(::FiatRatesSyncWorker)
}
