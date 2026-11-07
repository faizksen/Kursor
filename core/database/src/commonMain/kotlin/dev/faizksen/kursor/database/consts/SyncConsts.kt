package dev.faizksen.kursor.database.consts

internal const val SYNC_SUCCESS = "success"
internal const val SYNC_FAILED = "failed"
internal const val SYNC_RETRY = "retry"

internal const val SYNC_TRIGGER_PERIODIC = "periodic"
internal const val SYNC_TRIGGER_MANUAL = "manual"

sealed interface SyncResultColumn {
    val columnValue: String
    data object Success: SyncResultColumn { override val columnValue = SYNC_SUCCESS }
    data object Retry: SyncResultColumn { override val columnValue = SYNC_RETRY }
    data object Failed: SyncResultColumn { override val columnValue = SYNC_FAILED }
    data class Unrecognized(override val columnValue: String): SyncResultColumn
}

sealed interface TriggerColumn {
    val columnValue: String
    data object Periodic: TriggerColumn { override val columnValue = SYNC_TRIGGER_PERIODIC }
    data object Manual: TriggerColumn { override val columnValue = SYNC_TRIGGER_MANUAL }
    data class Unrecognized(override val columnValue: String): TriggerColumn
}

internal const val NETWORK_TRANSIENT_ERROR_NO_NETWORK = "NoNetwork"
internal const val NETWORK_TRANSIENT_ERROR_TIMEOUT = "Timeout"
internal const val NETWORK_TRANSIENT_ERROR_SERVER = "ServerError"
internal const val NETWORK_TRANSIENT_ERROR_RATE_LIMITED = "RateLimited"

internal const val NETWORK_PERMANENT_ERROR_INVALID_CURRENCY = "InvalidCurrency"
internal const val NETWORK_PERMANENT_ERROR_PARSE = "ParseError"
internal const val NETWORK_PERMANENT_ERROR_UNKNOWN_SOURCE = "UnknownSource"
internal const val NETWORK_PERMANENT_ERROR_UNKNOWN = "Unknown"

sealed interface ErrorColumn {
    val columnValue: String
    data object NoNetwork: ErrorColumn { override val columnValue = NETWORK_TRANSIENT_ERROR_NO_NETWORK }
    data object Timeout: ErrorColumn { override val columnValue = NETWORK_TRANSIENT_ERROR_TIMEOUT }
    data object ServerError: ErrorColumn { override val columnValue = NETWORK_TRANSIENT_ERROR_SERVER }
    data object RateLimited: ErrorColumn { override val columnValue = NETWORK_TRANSIENT_ERROR_RATE_LIMITED }
    data object InvalidCurrency: ErrorColumn { override val columnValue = NETWORK_PERMANENT_ERROR_INVALID_CURRENCY }
    data object ParseError: ErrorColumn { override val columnValue = NETWORK_PERMANENT_ERROR_PARSE }
    data object UnknownSource: ErrorColumn { override val columnValue = NETWORK_PERMANENT_ERROR_UNKNOWN_SOURCE }
    data object Unknown: ErrorColumn { override val columnValue = NETWORK_PERMANENT_ERROR_UNKNOWN }
    data class Unrecognized(override val columnValue: String): ErrorColumn
}
