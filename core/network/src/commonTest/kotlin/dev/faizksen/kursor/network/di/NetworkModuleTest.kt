package dev.faizksen.kursor.network.di

import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.FrankfurterSource
import dev.faizksen.kursor.network.RatesRemoteSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NetworkModuleTest {

    private val koin: Koin = koinApplication {
        modules(
            networkModule,
            module { single { HttpClient(MockEngine { respondOk() }) } },
        )
    }.koin

    @AfterTest
    fun tearDown() {
        koin.close()
    }

    @Test
    fun `the source map holds every source the app knows`() {
        assertEquals(
            setOf(SourceId.FRANKFURTER),
            koin.get<Map<SourceId, RatesRemoteSource>>().keys,
        )
    }

    @Test
    fun `the mapped source is the singleton the graph holds`() {
        assertSame(
            koin.get<FrankfurterSource>(),
            koin.get<Map<SourceId, RatesRemoteSource>>()[SourceId.FRANKFURTER],
        )
    }

    @Test
    fun `the shared json tolerates fields the app does not know`() {
        assertTrue(koin.get<Json>().configuration.ignoreUnknownKeys)
    }
}
