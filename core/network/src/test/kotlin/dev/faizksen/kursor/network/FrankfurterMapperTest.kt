package dev.faizksen.kursor.network

import com.google.common.truth.Truth.assertThat
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.network.dto.FrankfurterRateDto
import dev.faizksen.kursor.network.util.JsonCreator
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

class FrankfurterMapperTest {
    companion object {
        private const val REAL_RESPONSE =
            """[{"date":"2026-09-05","base":"EUR","quote":"PLN","rate":4.3244},{"date":"2026-09-05","base":"EUR","quote":"USD","rate":1.1627}]"""
    }

    @Test
    fun `maps real API response to RemoteRate`() {
        val json = JsonCreator.create()
        val realJson = json.decodeFromString<List<FrankfurterRateDto>>(REAL_RESPONSE)
            .map(FrankfurterRateDto::toRemoteRate)

        assertThat(realJson).hasSize(2)
        assertThat(realJson[0].base).isEqualTo(CurrencyCode("EUR"))
        assertThat(realJson[0].quote).isEqualTo(CurrencyCode("PLN"))
        assertThat(realJson[0].date).isEqualTo(LocalDate.of(2026,9,5))
        assertThat(realJson[0].value).isEqualTo(BigDecimal("4.3244"))
    }

    @Test(expected = DateTimeParseException::class)
    fun `fails on malformed date`() {
        FrankfurterRateDto(
            date = "05.09.2026",
            base = "EUR",
            quote = "PLN",
            rate = 4.3244,
        ).toRemoteRate()
    }

    @Test
    fun `maps set of CurrencyCode to quotes params`() {
        val codeSet = setOf(
            CurrencyCode("PLN"),
            CurrencyCode("USD"),
            CurrencyCode("EUR")
        )
        val result = codeSet.toQueryParam()
        assertThat(result.split(",")).containsExactly("PLN", "USD", "EUR")
    }
}

