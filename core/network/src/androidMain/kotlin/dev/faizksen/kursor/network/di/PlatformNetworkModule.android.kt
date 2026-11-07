package dev.faizksen.kursor.network.di

import android.content.Context
import android.content.pm.ApplicationInfo
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.Logger
import kotlinx.io.files.Path
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformNetworkModule: Module = module {
    single {
        val context = get<Context>()

        httpClient(
            cacheDirectory = Path(context.cacheDir.path, HTTP_CACHE_DIRECTORY),
            loggingEnabled = context.isDebuggable(),
            httpLogger = Logger.ANDROID,
            json = get(),
        )
    }
}

private fun Context.isDebuggable(): Boolean =
    applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
