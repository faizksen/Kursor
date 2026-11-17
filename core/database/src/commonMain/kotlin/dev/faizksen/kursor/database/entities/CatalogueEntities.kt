package dev.faizksen.kursor.database.entities

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import dev.faizksen.kursor.database.consts.CadenceColumn
import dev.faizksen.kursor.database.consts.CurrencyTypeColumn

@Entity(tableName = "currency")
data class CurrencyEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val symbol: String,
    val type: CurrencyTypeColumn,
)

@Entity(tableName = "provider")
data class ProviderEntity(
    @PrimaryKey
    val providerKey: String,
    val name: String,
    val rateType: String?,
    val pivotCurrency: String,
    val cadence: CadenceColumn,
    val latestDate: String?,
    val dataUrl: String?,
)

@Entity(
    tableName = "provider_coverage",
    primaryKeys = ["providerKey", "code"],
    indices = [Index(value = ["code"])],
)
data class ProviderCoverageEntity(
    val providerKey: String,
    val code: String,
)

@Entity(tableName = "catalogue_meta")
data class CatalogueMetaEntity(
    @PrimaryKey
    val id: Int = SINGLE_ROW,
    val fetchedAt: Long,
) {
    companion object {
        const val SINGLE_ROW = 0
    }
}
