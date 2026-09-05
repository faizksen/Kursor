package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.network.dto.FrankfurterRateDto
import dev.faizksen.kursor.network.model.RemoteRate
import java.time.LocalDate
import javax.inject.Inject

class FrankfurterSource @Inject constructor(
    private val apiInterface: ApiInterfaceFrankfurter
): RatesRemoteSource {
    override val id: SourceId
        get() = SourceId.FRANKFURTER

    override suspend fun latest(
        base: CurrencyCode,
        symbols: Set<CurrencyCode>
    ): List<RemoteRate> = apiInterface.rates(
        base = base.code,
        quotes = symbols.toQueryParam()
    ).map(FrankfurterRateDto::toRemoteRate)
}

internal fun Set<CurrencyCode>.toQueryParam(): String = joinToString(",") { it.code }

internal fun FrankfurterRateDto.toRemoteRate() = RemoteRate(
    base = CurrencyCode(base),
    quote = CurrencyCode(quote),
    date = LocalDate.parse(date),
    value = rate.toBigDecimal(),
)
