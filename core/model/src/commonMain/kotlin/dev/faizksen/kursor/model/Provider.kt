package dev.faizksen.kursor.model

import kotlinx.datetime.LocalDate
import kotlin.jvm.JvmInline

@JvmInline
value class ProviderKey(val key: String) {
    init {
        require(key.isNotBlank()) { "Provider key must not be blank" }
    }
}

enum class PublishCadence {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    UNKNOWN,
}

data class Provider(
    val key: ProviderKey,
    val name: String,
    val rateType: String?,
    val pivotCurrency: CurrencyCode,
    val cadence: PublishCadence,
    val latestDate: LocalDate?,
    val dataUrl: String?,
)
