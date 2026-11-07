package dev.faizksen.kursor.network.di

import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.SIMPLE
import kotlinx.io.files.Path
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual val platformNetworkModule: Module = module {
    single {
        httpClient(
            cacheDirectory = Path(cachesDirectory(), HTTP_CACHE_DIRECTORY),
            loggingEnabled = false,
            httpLogger = Logger.SIMPLE,
            json = get(),
        )
    }
}

private fun cachesDirectory(): String = NSSearchPathForDirectoriesInDomains(
    directory = NSCachesDirectory,
    domainMask = NSUserDomainMask,
    expandTilde = true,
).first() as String
