package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyType
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.network.util.JsonCreator
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CatalogueSourceTest {

    private val requested = mutableListOf<String>()

    private val engine = MockEngine { request ->
        requested += request.url.encodedPathAndQuery
        respond(
            content = bodyFor(request.url.encodedPath),
            status = HttpStatusCode.OK,
            headers = headersOf(
                HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
            ),
        )
    }

    private val source = CatalogueSource(
        HttpClient(engine) {
            expectSuccess = true
            defaultRequest { url(BASE_URL) }
            install(ContentNegotiation) { json(JsonCreator.create()) }
        }
    )

    @Test
    fun readsCurrenciesWithTheirType() = runTest {
        val catalogue = source.fetch()

        assertEquals(3, catalogue.currencies.size)

        val pln = catalogue.currencies.single { currency -> currency.code == CurrencyCode("PLN") }
        assertEquals("Polish Zloty", pln.name)
        assertEquals("zl", pln.symbol)
        assertEquals(CurrencyType.FIAT, pln.type)

        assertEquals(
            CurrencyType.CRYPTO,
            catalogue.currencies.single { it.code == CurrencyCode("BTC") }.type,
        )
        assertEquals(
            CurrencyType.METAL,
            catalogue.currencies.single { it.code == CurrencyCode("XAU") }.type,
        )
    }

    @Test
    fun fallsBackToTheCodeWhenACurrencyHasNoSymbol() = runTest {
        val catalogue = source.fetch()

        assertEquals("XAU", catalogue.currencies.single { it.code == CurrencyCode("XAU") }.symbol)
    }

    @Test
    fun readsProviderMetadata() = runTest {
        val catalogue = source.fetch()

        val ecb = catalogue.providers.single { provider -> provider.key == ProviderKey("ECB") }

        assertEquals("European Central Bank", ecb.name)
        assertEquals("reference rate", ecb.rateType)
        assertEquals(CurrencyCode("EUR"), ecb.pivotCurrency)
        assertEquals(PublishCadence.DAILY, ecb.cadence)
        assertEquals(LocalDate.parse("2026-11-17"), ecb.latestDate)
    }

    @Test
    fun aProviderThatDeclaresNoRateTypeKeepsItNull() = runTest {
        val catalogue = source.fetch()

        assertNull(catalogue.providers.single { it.key == ProviderKey("HNB") }.rateType)
    }

    @Test
    fun anUnknownCadenceBecomesUnknownRatherThanADefault() = runTest {
        val catalogue = source.fetch()

        assertEquals(
            PublishCadence.UNKNOWN,
            catalogue.providers.single { it.key == ProviderKey("HNB") }.cadence,
        )
    }

    @Test
    fun coverageListsEveryProviderQuotingACurrency() = runTest {
        val catalogue = source.fetch()

        val quotingPln = catalogue.coverage
            .filter { coverage -> coverage.currency == CurrencyCode("PLN") }
            .map { coverage -> coverage.provider }

        assertEquals(setOf(ProviderKey("ECB"), ProviderKey("NBP")), quotingPln.toSet())
    }

    @Test
    fun everyProviderInTheDocumentAlsoCoversItsBase() = runTest {
        val catalogue = source.fetch()

        val quotingEur = catalogue.coverage
            .filter { coverage -> coverage.currency == CurrencyCode("EUR") }
            .map { coverage -> coverage.provider }

        assertEquals(setOf(ProviderKey("ECB"), ProviderKey("NBP"), ProviderKey("HNB")), quotingEur.toSet())
    }

    @Test
    fun coverageIsReadFromTheExpandedDocumentForASingleBase() = runTest {
        source.fetch()

        assertTrue(
            requested.any { path -> path.contains("base=EUR") && path.contains("expand=providers") },
            "coverage must come from one expanded request, got $requested",
        )
    }

    private fun bodyFor(path: String): String = when {
        path.endsWith(CURRENCIES) -> CURRENCIES_BODY
        path.endsWith(PROVIDERS) -> PROVIDERS_BODY
        else -> COVERAGE_BODY
    }

    private companion object {
        const val BASE_URL = "https://example.invalid/v2/"
        const val CURRENCIES = "currencies"
        const val PROVIDERS = "providers"

        val CURRENCIES_BODY = """
            [
              { "iso_code": "PLN", "name": "Polish Zloty", "symbol": "zl", "type": "currency" },
              { "iso_code": "BTC", "name": "Bitcoin", "symbol": "B", "type": "crypto" },
              { "iso_code": "XAU", "name": "Gold (Troy Ounce)", "type": "metal" }
            ]
        """.trimIndent()

        val PROVIDERS_BODY = """
            [
              {
                "key": "ECB",
                "name": "European Central Bank",
                "rate_type": "reference rate",
                "pivot_currency": "EUR",
                "publish_cadence": "daily",
                "end_date": "2026-11-17",
                "data_url": "https://ecb.example.invalid"
              },
              {
                "key": "NBP",
                "name": "Narodowy Bank Polski",
                "rate_type": "official rate",
                "pivot_currency": "PLN",
                "publish_cadence": "daily",
                "end_date": "2026-11-17"
              },
              {
                "key": "HNB",
                "name": "Croatian National Bank",
                "rate_type": null,
                "pivot_currency": "EUR",
                "end_date": null
              }
            ]
        """.trimIndent()

        val COVERAGE_BODY = """
            [
              {
                "date": "2026-11-17",
                "base": "EUR",
                "quote": "PLN",
                "rate": 4.41,
                "providers": [ { "key": "ECB" }, { "key": "NBP" } ]
              },
              {
                "date": "2026-11-17",
                "base": "EUR",
                "quote": "USD",
                "rate": 1.1,
                "providers": [ { "key": "ECB" }, { "key": "HNB" } ]
              }
            ]
        """.trimIndent()
    }
}
