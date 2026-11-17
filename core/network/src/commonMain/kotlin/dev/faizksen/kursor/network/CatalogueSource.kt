package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.Catalogue
import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyType
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderCoverage
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.network.dto.CurrencyDto
import dev.faizksen.kursor.network.dto.ExpandedRateDto
import dev.faizksen.kursor.network.dto.ProviderDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate

internal class CatalogueSource(
    private val httpClient: HttpClient,
) : CatalogueRemoteSource {
    override suspend fun fetch(): Catalogue = remoteCall {
        Catalogue(
            currencies = currencies(),
            providers = providers(),
            coverage = coverage(),
        )
    }

    private suspend fun currencies(): List<Currency> = httpClient
        .get(CURRENCIES_PATH)
        .body<List<CurrencyDto>>()
        .map(CurrencyDto::toCurrency)

    private suspend fun providers(): List<Provider> = httpClient
        .get(PROVIDERS_PATH)
        .body<List<ProviderDto>>()
        .map(ProviderDto::toProvider)

    private suspend fun coverage(): List<ProviderCoverage> = httpClient
        .get(RATES_PATH) {
            parameter(BASE_PARAM, COVERAGE_BASE)
            parameter(EXPAND_PARAM, EXPAND_PROVIDERS)
        }
        .body<List<ExpandedRateDto>>()
        .toCoverage()
}

internal fun CurrencyDto.toCurrency(): Currency = Currency(
    code = CurrencyCode(isoCode),
    name = name,
    symbol = symbol ?: isoCode,
    type = type.toCurrencyType(),
)

internal fun ProviderDto.toProvider(): Provider = Provider(
    key = ProviderKey(key),
    name = name,
    rateType = rateType?.takeIf { it.isNotBlank() },
    pivotCurrency = CurrencyCode(pivotCurrency),
    cadence = publishCadence.toCadence(),
    latestDate = endDate?.let { date -> LocalDate.parse(date) },
    dataUrl = dataUrl,
)

internal fun List<ExpandedRateDto>.toCoverage(): List<ProviderCoverage> {
    val quoted = flatMap { rate ->
        rate.providers.map { provider ->
            ProviderCoverage(
                provider = ProviderKey(provider.key),
                currency = CurrencyCode(rate.quote),
            )
        }
    }

    val base = quoted
        .map { coverage -> coverage.provider }
        .distinct()
        .map { provider -> ProviderCoverage(provider, CurrencyCode(COVERAGE_BASE)) }

    return quoted + base
}

private fun String.toCurrencyType(): CurrencyType = when (this) {
    TYPE_FIAT -> CurrencyType.FIAT
    TYPE_CRYPTO -> CurrencyType.CRYPTO
    TYPE_METAL -> CurrencyType.METAL
    else -> CurrencyType.OTHER
}

private fun String?.toCadence(): PublishCadence = when (this) {
    CADENCE_DAILY -> PublishCadence.DAILY
    CADENCE_WEEKLY -> PublishCadence.WEEKLY
    CADENCE_MONTHLY -> PublishCadence.MONTHLY
    CADENCE_QUARTERLY -> PublishCadence.QUARTERLY
    else -> PublishCadence.UNKNOWN
}

private const val CURRENCIES_PATH = "currencies"
private const val PROVIDERS_PATH = "providers"
private const val RATES_PATH = "rates"
private const val BASE_PARAM = "base"
private const val EXPAND_PARAM = "expand"
private const val EXPAND_PROVIDERS = "providers"

internal const val COVERAGE_BASE = "EUR"

private const val TYPE_FIAT = "currency"
private const val TYPE_CRYPTO = "crypto"
private const val TYPE_METAL = "metal"

private const val CADENCE_DAILY = "daily"
private const val CADENCE_WEEKLY = "weekly"
private const val CADENCE_MONTHLY = "monthly"
private const val CADENCE_QUARTERLY = "quarterly"
