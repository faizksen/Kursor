package dev.faizksen.kursor.database

import androidx.room3.ColumnTypeConverter
import dev.faizksen.kursor.database.consts.CadenceColumn
import dev.faizksen.kursor.database.consts.CurrencyTypeColumn
import dev.faizksen.kursor.database.consts.ErrorColumn
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.consts.SyncResultColumn
import dev.faizksen.kursor.database.consts.TriggerColumn

class SyncLogConverters {
    @ColumnTypeConverter
    fun fromResult(result: SyncResultColumn): String = result.columnValue

    @ColumnTypeConverter
    fun toResult(value: String): SyncResultColumn = when (value) {
        SyncResultColumn.Success.columnValue -> SyncResultColumn.Success
        SyncResultColumn.Retry.columnValue -> SyncResultColumn.Retry
        SyncResultColumn.Failed.columnValue -> SyncResultColumn.Failed
        else -> SyncResultColumn.Unrecognized(value)
    }

    @ColumnTypeConverter
    fun fromTrigger(trigger: TriggerColumn): String = trigger.columnValue

    @ColumnTypeConverter
    fun toTrigger(value: String): TriggerColumn = when (value) {
        TriggerColumn.Periodic.columnValue -> TriggerColumn.Periodic
        TriggerColumn.Manual.columnValue -> TriggerColumn.Manual
        else -> TriggerColumn.Unrecognized(value)
    }

    @ColumnTypeConverter
    fun fromErrorColumn(column: ErrorColumn): String = column.columnValue

    @ColumnTypeConverter
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
    @ColumnTypeConverter
    fun fromSourceId(column: SourceIdColumn): String = column.columnValue

    @ColumnTypeConverter
    fun toSourceIdColumn(value: String): SourceIdColumn = when (value) {
        SourceIdColumn.Frankfurter.columnValue -> SourceIdColumn.Frankfurter
        SourceIdColumn.CoinGecko.columnValue -> SourceIdColumn.CoinGecko
        SourceIdColumn.CBR.columnValue -> SourceIdColumn.CBR
        else -> SourceIdColumn.Unrecognized(value)
    }
}

class CatalogueConverters {
    @ColumnTypeConverter
    fun fromCurrencyType(column: CurrencyTypeColumn): String = column.columnValue

    @ColumnTypeConverter
    fun toCurrencyTypeColumn(value: String): CurrencyTypeColumn = when (value) {
        CurrencyTypeColumn.Fiat.columnValue -> CurrencyTypeColumn.Fiat
        CurrencyTypeColumn.Crypto.columnValue -> CurrencyTypeColumn.Crypto
        CurrencyTypeColumn.Metal.columnValue -> CurrencyTypeColumn.Metal
        CurrencyTypeColumn.Other.columnValue -> CurrencyTypeColumn.Other
        else -> CurrencyTypeColumn.Unrecognized(value)
    }

    @ColumnTypeConverter
    fun fromCadence(column: CadenceColumn): String = column.columnValue

    @ColumnTypeConverter
    fun toCadenceColumn(value: String): CadenceColumn = when (value) {
        CadenceColumn.Daily.columnValue -> CadenceColumn.Daily
        CadenceColumn.Weekly.columnValue -> CadenceColumn.Weekly
        CadenceColumn.Monthly.columnValue -> CadenceColumn.Monthly
        CadenceColumn.Quarterly.columnValue -> CadenceColumn.Quarterly
        CadenceColumn.Unknown.columnValue -> CadenceColumn.Unknown
        else -> CadenceColumn.Unrecognized(value)
    }
}
