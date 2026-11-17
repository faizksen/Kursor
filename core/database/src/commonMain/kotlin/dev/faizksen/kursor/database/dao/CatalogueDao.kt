package dev.faizksen.kursor.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import dev.faizksen.kursor.database.consts.CurrencyTypeColumn
import dev.faizksen.kursor.database.entities.CatalogueMetaEntity
import dev.faizksen.kursor.database.entities.CurrencyEntity
import dev.faizksen.kursor.database.entities.ProviderCoverageEntity
import dev.faizksen.kursor.database.entities.ProviderEntity

@Dao
interface CatalogueDao {

    @Transaction
    suspend fun replaceAll(
        currencies: List<CurrencyEntity>,
        providers: List<ProviderEntity>,
        coverage: List<ProviderCoverageEntity>,
        fetchedAt: Long,
    ) {
        deleteCurrencies()
        deleteProviders()
        deleteCoverage()
        deleteMeta()

        insertCurrencies(currencies)
        insertProviders(providers)
        insertCoverage(coverage)
        insertMeta(CatalogueMetaEntity(fetchedAt = fetchedAt))
    }

    @Query("SELECT fetchedAt FROM catalogue_meta WHERE id = ${CatalogueMetaEntity.SINGLE_ROW}")
    suspend fun fetchedAt(): Long?

    @Query("SELECT * FROM currency WHERE type = :type ORDER BY code ASC")
    suspend fun currenciesOfType(type: CurrencyTypeColumn): List<CurrencyEntity>

    @Query("SELECT * FROM provider WHERE providerKey IN (:keys)")
    suspend fun providers(keys: List<String>): List<ProviderEntity>

    @Query("SELECT providerKey FROM provider_coverage WHERE code = :code")
    suspend fun providersCovering(code: String): List<String>

    @Insert
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)

    @Insert
    suspend fun insertProviders(providers: List<ProviderEntity>)

    @Insert
    suspend fun insertCoverage(coverage: List<ProviderCoverageEntity>)

    @Insert
    suspend fun insertMeta(meta: CatalogueMetaEntity)

    @Query("DELETE FROM currency")
    suspend fun deleteCurrencies()

    @Query("DELETE FROM provider")
    suspend fun deleteProviders()

    @Query("DELETE FROM provider_coverage")
    suspend fun deleteCoverage()

    @Query("DELETE FROM catalogue_meta")
    suspend fun deleteMeta()
}
