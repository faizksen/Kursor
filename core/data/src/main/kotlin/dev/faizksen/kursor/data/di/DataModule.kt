package dev.faizksen.kursor.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.data.RatesRepositoryImpl
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindRatesRepository(impl: RatesRepositoryImpl): RatesRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ClockModule {

    @Provides
    @Singleton
    fun clock(): Clock = Clock.systemUTC()
}
