package dev.faizksen.kursor.common.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

@JvmInline
value class IoDispatcher(val dispatcher: CoroutineDispatcher)

@JvmInline
value class DefaultDispatcher(val dispatcher: CoroutineDispatcher)

@JvmInline
value class MainDispatcher(val dispatcher: CoroutineDispatcher)

val dispatchersModule = module {
    single { IoDispatcher(Dispatchers.IO) }
    single { DefaultDispatcher(Dispatchers.Default) }
    single { MainDispatcher(Dispatchers.Main.immediate) }
}
