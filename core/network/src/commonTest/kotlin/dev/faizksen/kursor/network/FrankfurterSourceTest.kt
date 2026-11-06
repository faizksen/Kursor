package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.network.util.JsonCreator
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FrankfurterSourceTest {

    private val queued = mutableListOf<Pair<HttpStatusCode, String>>()

    private val engine = MockEngine {
        val (status, body) = queued.removeAt(0)
        respond(
            content = body,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }

    private val source = FrankfurterSource(
        HttpClient(engine) {
            expectSuccess = true
            defaultRequest { url(BASE_URL) }
            install(ContentNegotiation) { json(JsonCreator.create()) }
        }
    )

    private fun enqueue(code: Int, body: String) {
        queued += HttpStatusCode.fromValue(code) to body
    }

    @Test
    fun mapsResponseToRemoteRates() = runTest {
        enqueue(200, EUR_RESPONSE)

        val result = source.latest(listOf(EUR_PLN))

        assertEquals(1, result.size)
        assertEquals(CurrencyCode("EUR"), result[0].base)
        assertEquals(CurrencyCode("PLN"), result[0].quote)
        assertEquals(LocalDate(2026, 9, 5), result[0].date)
        assertEquals(Rate.parse("4.3244"), result[0].value)
    }

    @Test
    fun sendsBaseAndCommaSeparatedQuotes() = runTest {
        enqueue(200, EUR_RESPONSE)

        source.latest(listOf(EUR_PLN, EUR_USD))

        val parameters = engine.requestHistory.single().url.parameters
        assertEquals("EUR", parameters["base"])
        assertEquals("PLN,USD", parameters["quotes"])
    }

    @Test
    fun groupsPairsByBaseIntoSeparateRequests() = runTest {
        enqueue(200, EUR_RESPONSE)
        enqueue(200, USD_RESPONSE)

        val result = source.latest(listOf(EUR_PLN, USD_EUR))

        assertEquals(2, engine.requestHistory.size)
        assertEquals("EUR", engine.requestHistory[0].url.parameters["base"])
        assertEquals("USD", engine.requestHistory[1].url.parameters["base"])
        assertEquals(2, result.size)
    }

    @Test
    fun throwsOnTooManyRequests() = runTest {
        enqueue(429, "")

        assertFailsWith<ResponseException> { source.latest(listOf(EUR_PLN)) }
    }

    @Test
    fun throwsOnServerError() = runTest {
        enqueue(500, "")

        assertFailsWith<ResponseException> { source.latest(listOf(EUR_PLN)) }
    }

    companion object {
        private const val BASE_URL = "https://api.frankfurter.dev/v2/"

        private val EUR_PLN = CurrencyPair(CurrencyCode("EUR"), CurrencyCode("PLN"))
        private val EUR_USD = CurrencyPair(CurrencyCode("EUR"), CurrencyCode("USD"))
        private val USD_EUR = CurrencyPair(CurrencyCode("USD"), CurrencyCode("EUR"))

        private const val EUR_RESPONSE = """
            [{"date":"2026-09-05","base":"EUR","quote":"PLN","rate":4.3244}]
        """
        private const val USD_RESPONSE = """
            [{"date":"2026-09-07","base":"USD","quote":"EUR","rate":0.8603}]
        """
    }
}
