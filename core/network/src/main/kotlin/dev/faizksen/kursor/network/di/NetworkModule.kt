package dev.faizksen.kursor.network.di

import android.content.Context
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.ApiInterfaceFrankfurter
import dev.faizksen.kursor.network.FrankfurterSource
import dev.faizksen.kursor.network.RatesRemoteSource
import dev.faizksen.kursor.network.util.JsonCreator
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.io.File

val networkModule = module {
    singleOf(::okHttpClient)
    singleOf(JsonCreator::create)
    singleOf(::frankfurterApi)
    singleOf(::FrankfurterSource)

    single<Map<SourceId, RatesRemoteSource>> {
        mapOf(SourceId.FRANKFURTER to get<FrankfurterSource>())
    }
}

internal fun okHttpClient(context: Context): OkHttpClient = OkHttpClient.Builder()
    .apply { loggingInterceptors().forEach(::addInterceptor) }
    .cache(
        Cache(
            directory = File(context.cacheDir, HTTP_CACHE_DIRECTORY),
            maxSize = HTTP_CACHE_SIZE,
        )
    )
    .build()

internal fun frankfurterApi(client: OkHttpClient, json: Json): ApiInterfaceFrankfurter = Retrofit.Builder()
    .baseUrl(FRANKFURTER_BASE_URL)
    .client(client)
    .addConverterFactory(json.asConverterFactory(APPLICATION_JSON.toMediaType()))
    .build()
    .create()

private const val FRANKFURTER_BASE_URL = "https://api.frankfurter.dev/v2/"
private const val APPLICATION_JSON = "application/json"
private const val HTTP_CACHE_DIRECTORY = "http_cache"
private const val HTTP_CACHE_SIZE = 5L * 1024 * 1024
