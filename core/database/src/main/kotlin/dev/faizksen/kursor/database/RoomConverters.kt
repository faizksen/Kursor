package dev.faizksen.kursor.database

import androidx.room.TypeConverter
import dev.faizksen.kursor.database.consts.ErrorColumn
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.consts.SyncResultColumn
import dev.faizksen.kursor.database.consts.TriggerColumn

class SyncLogConverters {
    @TypeConverter
    fun fromResult(result: SyncResultColumn): String = result.columnValue

    @TypeConverter
    fun toResult(value: String): SyncResultColumn = when (value) {
        SyncResultColumn.Success.columnValue -> SyncResultColumn.Success
        SyncResultColumn.Retry.columnValue -> SyncResultColumn.Retry
        SyncResultColumn.Failed.columnValue -> SyncResultColumn.Failed
        else -> SyncResultColumn.Unrecognized(value)
    }

    @TypeConverter
    fun fromTrigger(trigger: TriggerColumn): String = trigger.columnValue

    @TypeConverter
    fun toTrigger(value: String): TriggerColumn = when (value) {
        TriggerColumn.Periodic.columnValue -> TriggerColumn.Periodic
        TriggerColumn.Manual.columnValue -> TriggerColumn.Manual
        else -> TriggerColumn.Unrecognized(value)
    }

    @TypeConverter
    fun fromErrorColumn(column: ErrorColumn): String = column.columnValue

    @TypeConverter
    fun toErrorColumn(value: String): ErrorColumn = when (value) {
        ErrorColumn.NoNetwork.columnValue -> ErrorColumn.NoNetwork
        ErrorColumn.Timeout.columnValue -> ErrorColumn.Timeout
        ErrorColumn.ServerError.columnValue -> ErrorColumn.ServerError
        ErrorColumn.RateLimited.columnValue -> ErrorColumn.RateLimited
        ErrorColumn.InvalidCurrency.columnValue -> ErrorColumn.InvalidCurrency
        ErrorColumn.ParseError.columnValue -> ErrorColumn.ParseError
        ErrorColumn.UnknownSource.columnValue -> ErrorColumn.UnknownSource
        ErrorColumn.Unknown.columnValue -> ErrorColumn.Unknown
        else -> ErrorColumn.Unrecognized(value)
    }
}

class RateConverters {
    @TypeConverter
    fun fromSourceId(column: SourceIdColumn): String = column.columnValue

    @TypeConverter
    fun toSourceIdColumn(value: String): SourceIdColumn = when (value) {
        SourceIdColumn.Frankfurter.columnValue -> SourceIdColumn.Frankfurter
        SourceIdColumn.CoinGecko.columnValue -> SourceIdColumn.CoinGecko
        SourceIdColumn.CBR.columnValue -> SourceIdColumn.CBR
        else -> SourceIdColumn.Unrecognized(value)
    }
}
