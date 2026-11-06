package dev.faizksen.kursor.network.di

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

internal fun loggingInterceptors(): List<Interceptor> = listOf(
    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
)
