package dev.faizksen.kursor.model

@JvmInline
value class CurrencyCode(val code: String) // "EUR", "RUB", "BTC

enum class CurrencyType {
    FIAT,
    CRYPTO
}

data class Currency(
    val code: CurrencyCode,
    val name: String,
    val symbol: String, // "€", "₽", "$"
    val type: CurrencyType,
    val decimals: Int //count of numbers after dot
)