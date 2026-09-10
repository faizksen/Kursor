package dev.faizksen.kursor.network

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.network.util.JsonCreator
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.math.BigDecimal
import java.time.LocalDate

class FrankfurterSourceTest {
    private lateinit var webServer: MockWebServer
    private lateinit var source: FrankfurterSource
    private val json = JsonCreator.create()

    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.start()
        val apiInterface = Retrofit.Builder()
            .baseUrl(webServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiInterfaceFrankfurter::class.java)
        source = FrankfurterSource(apiInterface)
    }

    @After
    fun tearDown() {
        webServer.close()
    }

    private fun enqueue(code: Int, body: String) {
        webServer.enqueue(
            MockResponse.Builder()
                .code(code)
                .body(body)
                .build()
        )
    }

    @Test
    fun mapsResponseToRemoteRates() = runTest {
        enqueue(200, EUR_RESPONSE)

        val result = source.latest(listOf(EUR_PLN))

        assertThat(result).hasSize(1)
        assertThat(result[0].base).isEqualTo(CurrencyCode("EUR"))
        assertThat(result[0].quote).isEqualTo(CurrencyCode("PLN"))
        assertThat(result[0].date).isEqualTo(LocalDate.of(2026, 9, 5))
        assertThat(result[0].value).isEqualTo(BigDecimal("4.3244"))
    }

    @Test
    fun sendsBaseAndCommaSeparatedQuotes() = runTest {
        enqueue(200, EUR_RESPONSE)

        source.latest(listOf(EUR_PLN, EUR_USD))

        val path = webServer.takeRequest().url.toString()
        assertThat(path).contains("base=EUR")
        assertThat(path).contains("quotes=PLN%2CUSD")
    }

    @Test
    fun groupsPairsByBaseIntoSeparateRequests() = runTest {
        enqueue(200, EUR_RESPONSE)
        enqueue(200, USD_RESPONSE)

        val result = source.latest(listOf(EUR_PLN, USD_EUR))

        assertThat(webServer.requestCount).isEqualTo(2)
        assertThat(webServer.takeRequest().url.toString()).contains("base=EUR")
        assertThat(webServer.takeRequest().url.toString()).contains("base=USD")
        assertThat(result).hasSize(2)
    }

    @Test(expected = HttpException::class)
    fun throwsOnTooManyRequests() = runTest {
        enqueue(429, "")

        source.latest(listOf(EUR_PLN))
    }

    @Test(expected = HttpException::class)
    fun throwsOnServerError() = runTest {
        enqueue(500, "")

        source.latest(listOf(EUR_PLN))
    }

    companion object {
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
