package dev.faizksen.kursor.network.di

import android.content.Context
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.ApiInterfaceFrankfurter
import dev.faizksen.kursor.network.util.JsonCreator
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface InterceptorsModule {

    @Multibinds
    fun interceptors(): Set<Interceptor>
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun baseOkHttp(
        interceptors: Set<@JvmSuppressWildcards Interceptor>,
        @ApplicationContext context: Context
    ): OkHttpClient = OkHttpClient.Builder()
        .apply { interceptors.forEach(::addInterceptor) }
        .cache(
            Cache(
                directory = File(context.cacheDir, "http_cache"),
                maxSize = 5L * 1024 * 1024,
            )
        )
        .build()

    @Provides
    @Singleton
    fun json(): Json = JsonCreator.create()

    @Provides
    @Singleton
    fun frankfurterApi(
        client: OkHttpClient,
        json: Json
    ): ApiInterfaceFrankfurter = Retrofit.Builder()
        .baseUrl("https://api.frankfurter.dev/v2/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()
}
