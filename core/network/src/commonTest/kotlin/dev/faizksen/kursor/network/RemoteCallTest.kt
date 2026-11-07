package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.network.model.RemoteSourceException
import dev.faizksen.kursor.network.util.JsonCreator
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteCallTest {

    @Test
    fun reportsTransportFailureAsNoNetwork() = runTest {
        val cause = IOException("unable to resolve host")

        val failure = assertFailsWith<RemoteSourceException.NoNetwork> {
            sourceThrowing(cause).latest(listOf(EUR_PLN))
        }

        assertTrue(failure.cause is IOException)
        assertEquals(cause.message, failure.cause?.message)
    }

    @Test
    fun reportsConnectTimeoutAsTimeout() = runTest {
        val cause = ConnectTimeoutException("connect timed out", null)

        val failure = assertFailsWith<RemoteSourceException.Timeout> {
            sourceThrowing(cause).latest(listOf(EUR_PLN))
        }

        assertEquals(cause.message, failure.cause?.message)
    }

    @Test
    fun reportsRequestTimeoutAsTimeout() = runTest {
        assertFailsWith<RemoteSourceException.Timeout> {
            sourceThrowing(HttpRequestTimeoutException(BASE_URL, 1_000L)).latest(listOf(EUR_PLN))
        }
    }

    private fun sourceThrowing(cause: Throwable) = FrankfurterSource(
        HttpClient(MockEngine { throw cause }) {
            expectSuccess = true
            defaultRequest { url(BASE_URL) }
            install(ContentNegotiation) { json(JsonCreator.create()) }
        }
    )

    private companion object {
        const val BASE_URL = "https://api.frankfurter.dev/v2/"

        val EUR_PLN = CurrencyPair(CurrencyCode("EUR"), CurrencyCode("PLN"))
    }
}
