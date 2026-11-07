package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.network.dto.FrankfurterRateDto
import dev.faizksen.kursor.network.model.RemoteSourceException
import dev.faizksen.kursor.network.util.JsonCreator
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FrankfurterMapperTest {

    @Test
    fun `maps real API response to RemoteRate`() {
        val json = JsonCreator.create()
        val realJson = json.decodeFromString<List<FrankfurterRateDto>>(REAL_RESPONSE)
            .map(FrankfurterRateDto::toRemoteRate)

        assertEquals(2, realJson.size)
        assertEquals(CurrencyCode("EUR"), realJson[0].base)
        assertEquals(CurrencyCode("PLN"), realJson[0].quote)
        assertEquals(LocalDate(2026, 9, 5), realJson[0].date)
        assertEquals(Rate.parse("4.3244"), realJson[0].value)
    }

    @Test
    fun `fails on malformed date`() {
        assertFailsWith<RemoteSourceException.Malformed> {
            FrankfurterRateDto(
                date = "05.09.2026",
                base = "EUR",
                quote = "PLN",
                rate = 4.3244,
            ).toRemoteRate()
        }
    }

    @Test
    fun `maps set of CurrencyCode to quotes params`() {
        val codeSet = setOf(
            CurrencyCode("PLN"),
            CurrencyCode("USD"),
            CurrencyCode("EUR")
        )

        assertEquals(listOf("PLN", "USD", "EUR"), codeSet.toQueryParam().split(","))
    }

    companion object {
        private const val REAL_RESPONSE =
            """[{"date":"2026-09-05","base":"EUR","quote":"PLN","rate":4.3244},{"date":"2026-09-05","base":"EUR","quote":"USD","rate":1.1627}]"""
    }
}
