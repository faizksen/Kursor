package dev.faizksen.kursor.data.mappers

import dev.faizksen.kursor.database.consts.CadenceColumn
import dev.faizksen.kursor.database.consts.CurrencyTypeColumn
import dev.faizksen.kursor.database.entities.CurrencyEntity
import dev.faizksen.kursor.database.entities.ProviderCoverageEntity
import dev.faizksen.kursor.database.entities.ProviderEntity
import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyType
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderCoverage
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.PublishCadence
import kotlinx.datetime.LocalDate

internal fun Currency.toEntity(): CurrencyEntity = CurrencyEntity(
    code = code.code,
    name = name,
    symbol = symbol,
    type = type.toColumn(),
)

internal fun CurrencyEntity.toCurrency(): Currency = Currency(
    code = CurrencyCode(code),
    name = name,
    symbol = symbol,
    type = type.toCurrencyType(),
)

internal fun Provider.toEntity(): ProviderEntity = ProviderEntity(
    providerKey = key.key,
    name = name,
    rateType = rateType,
    pivotCurrency = pivotCurrency.code,
    cadence = cadence.toColumn(),
    latestDate = latestDate?.toString(),
    dataUrl = dataUrl,
)

internal fun ProviderEntity.toProvider(): Provider = Provider(
    key = ProviderKey(providerKey),
    name = name,
    rateType = rateType,
    pivotCurrency = CurrencyCode(pivotCurrency),
    cadence = cadence.toCadence(),
    latestDate = latestDate?.let { date -> LocalDate.parse(date) },
    dataUrl = dataUrl,
)

internal fun ProviderCoverage.toEntity(): ProviderCoverageEntity = ProviderCoverageEntity(
    providerKey = provider.key,
    code = currency.code,
)

internal fun CurrencyType.toColumn(): CurrencyTypeColumn = when (this) {
    CurrencyType.FIAT -> CurrencyTypeColumn.Fiat
    CurrencyType.CRYPTO -> CurrencyTypeColumn.Crypto
    CurrencyType.METAL -> CurrencyTypeColumn.Metal
    CurrencyType.OTHER -> CurrencyTypeColumn.Other
}

internal fun CurrencyTypeColumn.toCurrencyType(): CurrencyType = when (this) {
    CurrencyTypeColumn.Fiat -> CurrencyType.FIAT
    CurrencyTypeColumn.Crypto -> CurrencyType.CRYPTO
    CurrencyTypeColumn.Metal -> CurrencyType.METAL
    CurrencyTypeColumn.Other -> CurrencyType.OTHER
    is CurrencyTypeColumn.Unrecognized -> CurrencyType.OTHER
}

internal fun PublishCadence.toColumn(): CadenceColumn = when (this) {
    PublishCadence.DAILY -> CadenceColumn.Daily
    PublishCadence.WEEKLY -> CadenceColumn.Weekly
    PublishCadence.MONTHLY -> CadenceColumn.Monthly
    PublishCadence.QUARTERLY -> CadenceColumn.Quarterly
    PublishCadence.UNKNOWN -> CadenceColumn.Unknown
}

internal fun CadenceColumn.toCadence(): PublishCadence = when (this) {
    CadenceColumn.Daily -> PublishCadence.DAILY
    CadenceColumn.Weekly -> PublishCadence.WEEKLY
    CadenceColumn.Monthly -> PublishCadence.MONTHLY
    CadenceColumn.Quarterly -> PublishCadence.QUARTERLY
    CadenceColumn.Unknown -> PublishCadence.UNKNOWN
    is CadenceColumn.Unrecognized -> PublishCadence.UNKNOWN
}
