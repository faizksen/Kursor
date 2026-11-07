package dev.faizksen.kursor.network.di

import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.FrankfurterSource
import dev.faizksen.kursor.network.RatesRemoteSource
import dev.faizksen.kursor.network.util.JsonCreator
import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

expect val platformNetworkModule: Module

val networkModule = module {
    includes(platformNetworkModule)

    singleOf(JsonCreator::create)
    singleOf(::FrankfurterSource)

    single<Map<SourceId, RatesRemoteSource>> {
        mapOf(SourceId.FRANKFURTER to get<FrankfurterSource>())
    }
}

internal fun httpClient(
    cacheDirectory: Path,
    loggingEnabled: Boolean,
    httpLogger: Logger,
    json: Json,
): HttpClient {
    SystemFileSystem.createDirectories(cacheDirectory)

    return HttpClient {
        expectSuccess = true

        defaultRequest { url(FRANKFURTER_BASE_URL) }

        install(ContentNegotiation) { json(json) }

        install(HttpCache) { publicStorage(FileStorage(cacheDirectory)) }

        if (loggingEnabled) {
            install(Logging) {
                logger = httpLogger
                level = LogLevel.BODY
            }
        }
    }
}

internal const val HTTP_CACHE_DIRECTORY = "http_cache"

private const val FRANKFURTER_BASE_URL = "https://api.frankfurter.dev/v2/"
