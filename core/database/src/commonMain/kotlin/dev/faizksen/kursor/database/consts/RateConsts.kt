package dev.faizksen.kursor.database.consts

internal const val SOURCE_ID_FRANKFURTER = "FRANKFURTER"
internal const val SOURCE_ID_COIN_GECKO = "COIN_GECKO"
internal const val SOURCE_ID_CBR = "CBR"

sealed interface SourceIdColumn {
    val columnValue: String
    data object Frankfurter: SourceIdColumn { override val columnValue = SOURCE_ID_FRANKFURTER }
    data object CoinGecko: SourceIdColumn { override val columnValue = SOURCE_ID_COIN_GECKO }
    data object CBR: SourceIdColumn { override val columnValue = SOURCE_ID_CBR }
    data class Unrecognized(override val columnValue: String): SourceIdColumn
}
