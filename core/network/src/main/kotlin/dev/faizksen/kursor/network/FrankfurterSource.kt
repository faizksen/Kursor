package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.dto.FrankfurterRateDto
import dev.faizksen.kursor.network.model.RemoteRate
import dev.faizksen.kursor.network.model.RemoteRateFormatException
import kotlinx.datetime.LocalDate

class FrankfurterSource(
    private val apiInterface: ApiInterfaceFrankfurter
): RatesRemoteSource {
    override val id: SourceId
        get() = SourceId.FRANKFURTER

    override suspend fun latest(
        pairs: List<CurrencyPair>
    ): List<RemoteRate> = pairs.groupBy(
        keySelector = { it.base },
        valueTransform = { it.quote }
    ).flatMap { (base, quotes) ->
        apiInterface.rates(
            base = base.code,
            quotes = quotes.toQueryParam()
        ).map(FrankfurterRateDto::toRemoteRate)
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
