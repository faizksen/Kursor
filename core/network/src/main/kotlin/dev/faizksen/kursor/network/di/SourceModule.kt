package dev.faizksen.kursor.network.di

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.FrankfurterSource
import dev.faizksen.kursor.network.RatesRemoteSource

@Module
@InstallIn(SingletonComponent::class)
abstract class RatesRemoteSourcesModule {

    @MapKey
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RatesSourceKey(
        val value: SourceId
    )

    @Binds
    @IntoMap
    @RatesSourceKey(SourceId.FRANKFURTER)
    abstract fun bindEcbSource(
        source: FrankfurterSource
    ): RatesRemoteSource
}