package dev.faizksen.kursor.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyDto(
    @SerialName("iso_code") val isoCode: String,
    val name: String,
    val symbol: String? = null,
    val type: String,
)

@Serializable
data class ProviderDto(
    val key: String,
    val name: String,
    @SerialName("rate_type") val rateType: String? = null,
    @SerialName("pivot_currency") val pivotCurrency: String,
    @SerialName("publish_cadence") val publishCadence: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("data_url") val dataUrl: String? = null,
)

@Serializable
data class ExpandedRateDto(
    val quote: String,
    val providers: List<ProviderQuoteDto> = emptyList(),
)

@Serializable
data class ProviderQuoteDto(
    val key: String,
)
