package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.dto.FrankfurterRateDto
import dev.faizksen.kursor.network.model.RemoteRate
import dev.faizksen.kursor.network.model.RemoteRateFormatException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate

class FrankfurterSource(
    private val httpClient: HttpClient,
): RatesRemoteSource {
    override val id: SourceId
        get() = SourceId.FRANKFURTER

    override suspend fun latest(
        pairs: List<CurrencyPair>
    ): List<RemoteRate> = pairs.groupBy(
        keySelector = { it.base },
        valueTransform = { it.quote }
    ).flatMap { (base, quotes) ->
        httpClient.get(RATES_PATH) {
            parameter(BASE_PARAM, base.code)
            parameter(QUOTES_PARAM, quotes.toQueryParam())
        }
            .body<List<FrankfurterRateDto>>()
            .map(FrankfurterRateDto::toRemoteRate)
    }
}

internal fun Collection<CurrencyCode>.toQueryParam(): String = joinToString(",") { it.code }

internal fun FrankfurterRateDto.toRemoteRate(): RemoteRate = try {
    RemoteRate(
        base = CurrencyCode(base),
        quote = CurrencyCode(quote),
        date = LocalDate.parse(date),
        value = Rate.ofDouble(rate),
    )
} catch (e: IllegalArgumentException) {
    throw RemoteRateFormatException(e)
}

private const val RATES_PATH = "rates"
private const val BASE_PARAM = "base"
private const val QUOTES_PARAM = "quotes"
