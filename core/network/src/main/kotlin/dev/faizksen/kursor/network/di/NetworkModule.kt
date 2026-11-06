package dev.faizksen.kursor.network.di

import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.ApiInterfaceFrankfurter
import dev.faizksen.kursor.network.FrankfurterSource
import dev.faizksen.kursor.network.RatesRemoteSource
import dev.faizksen.kursor.network.util.JsonCreator
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.io.File

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .apply { loggingInterceptors().forEach(::addInterceptor) }
            .cache(
                Cache(
                    directory = File(androidContext().cacheDir, "http_cache"),
                    maxSize = HTTP_CACHE_SIZE,
                )
            )
            .build()
    }

    single<Json> { JsonCreator.create() }

    single<ApiInterfaceFrankfurter> {
        Retrofit.Builder()
            .baseUrl("https://api.frankfurter.dev/v2/")
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
    }

    singleOf(::FrankfurterSource)

    single<Map<SourceId, RatesRemoteSource>> {
        mapOf(SourceId.FRANKFURTER to get<FrankfurterSource>())
    }
}

private const val HTTP_CACHE_SIZE = 5L * 1024 * 1024
