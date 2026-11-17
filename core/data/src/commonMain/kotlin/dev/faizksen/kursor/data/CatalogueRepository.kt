package dev.faizksen.kursor.data

import dev.faizksen.kursor.data.mappers.toCurrency
import dev.faizksen.kursor.data.mappers.toEntity
import dev.faizksen.kursor.data.mappers.toProvider
import dev.faizksen.kursor.database.consts.CurrencyTypeColumn
import dev.faizksen.kursor.database.dao.CatalogueDao
import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.network.CatalogueRemoteSource
import kotlin.time.Clock
import kotlin.time.Instant

interface CatalogueRepository {
    suspend fun fetchedAt(): Instant?
    suspend fun refresh()
    suspend fun fiatCurrencies(): List<Currency>
    suspend fun providersFor(pair: CurrencyPair): List<Provider>
}

internal class CatalogueRepositoryImpl(
    private val remoteSource: CatalogueRemoteSource,
    private val catalogueDao: CatalogueDao,
    private val clock: Clock,
): CatalogueRepository {

    override suspend fun fetchedAt(): Instant? = catalogueDao
        .fetchedAt()
        ?.let { millis -> Instant.fromEpochMilliseconds(millis) }

    override suspend fun refresh() {
        val catalogue = remoteSource.fetch()

        catalogueDao.replaceAll(
            currencies = catalogue.currencies.map { currency -> currency.toEntity() },
            providers = catalogue.providers.map { provider -> provider.toEntity() },
            coverage = catalogue.coverage.map { coverage -> coverage.toEntity() },
            fetchedAt = clock.now().toEpochMilliseconds(),
        )
    }

    override suspend fun fiatCurrencies(): List<Currency> = catalogueDao
        .currenciesOfType(CurrencyTypeColumn.Fiat)
        .map { entity -> entity.toCurrency() }

    override suspend fun providersFor(pair: CurrencyPair): List<Provider> {
        val serving = catalogueDao.providersCovering(pair.base.code)
            .intersect(catalogueDao.providersCovering(pair.quote.code).toSet())

        return catalogueDao
            .providers(serving.toList())
            .map { entity -> entity.toProvider() }
    }
}
