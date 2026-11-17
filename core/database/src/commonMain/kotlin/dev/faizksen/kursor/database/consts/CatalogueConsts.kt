package dev.faizksen.kursor.database.consts

internal const val CURRENCY_TYPE_FIAT = "fiat"
internal const val CURRENCY_TYPE_CRYPTO = "crypto"
internal const val CURRENCY_TYPE_METAL = "metal"
internal const val CURRENCY_TYPE_OTHER = "other"

sealed interface CurrencyTypeColumn {
    val columnValue: String
    data object Fiat: CurrencyTypeColumn { override val columnValue = CURRENCY_TYPE_FIAT }
    data object Crypto: CurrencyTypeColumn { override val columnValue = CURRENCY_TYPE_CRYPTO }
    data object Metal: CurrencyTypeColumn { override val columnValue = CURRENCY_TYPE_METAL }
    data object Other: CurrencyTypeColumn { override val columnValue = CURRENCY_TYPE_OTHER }
    data class Unrecognized(override val columnValue: String): CurrencyTypeColumn
}

internal const val CADENCE_DAILY = "daily"
internal const val CADENCE_WEEKLY = "weekly"
internal const val CADENCE_MONTHLY = "monthly"
internal const val CADENCE_QUARTERLY = "quarterly"
internal const val CADENCE_UNKNOWN = "unknown"

sealed interface CadenceColumn {
    val columnValue: String
    data object Daily: CadenceColumn { override val columnValue = CADENCE_DAILY }
    data object Weekly: CadenceColumn { override val columnValue = CADENCE_WEEKLY }
    data object Monthly: CadenceColumn { override val columnValue = CADENCE_MONTHLY }
    data object Quarterly: CadenceColumn { override val columnValue = CADENCE_QUARTERLY }
    data object Unknown: CadenceColumn { override val columnValue = CADENCE_UNKNOWN }
    data class Unrecognized(override val columnValue: String): CadenceColumn
}
