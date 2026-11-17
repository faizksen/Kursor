package dev.faizksen.kursor.database

import dev.faizksen.kursor.database.consts.CadenceColumn
import dev.faizksen.kursor.database.consts.CurrencyTypeColumn
import dev.faizksen.kursor.database.dao.CatalogueDao
import dev.faizksen.kursor.database.entities.CurrencyEntity
import dev.faizksen.kursor.database.entities.ProviderCoverageEntity
import dev.faizksen.kursor.database.entities.ProviderEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CatalogueDaoTest {

    private lateinit var database: KursorDatabase
    private lateinit var dao: CatalogueDao

    @BeforeTest
    fun setUp() {
        database = testDatabase()
        dao = database.catalogueDao
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun anEmptyCatalogueHasNoFetchTime() = runTest {
        assertNull(dao.fetchedAt())
    }

    @Test
    fun replaceAllStoresTheCatalogueWithItsFetchTime() = runTest {
        dao.replaceAll(
            currencies = listOf(currency("PLN"), currency("BTC", CurrencyTypeColumn.Crypto)),
            providers = listOf(provider("NBP", pivot = "PLN")),
            coverage = listOf(coverage("NBP", "PLN")),
            fetchedAt = 1_000L,
        )

        assertEquals(1_000L, dao.fetchedAt())
        assertEquals(listOf("PLN"), dao.currenciesOfType(CurrencyTypeColumn.Fiat).map { it.code })
        assertEquals(listOf("NBP"), dao.providersCovering("PLN"))
    }

    @Test
    fun replaceAllDropsWhatTheEarlierCatalogueHeld() = runTest {
        dao.replaceAll(
            currencies = listOf(currency("PLN"), currency("CHF")),
            providers = listOf(provider("NBP", pivot = "PLN"), provider("ECB", pivot = "EUR")),
            coverage = listOf(coverage("NBP", "PLN"), coverage("ECB", "CHF")),
            fetchedAt = 1_000L,
        )

        dao.replaceAll(
            currencies = listOf(currency("PLN")),
            providers = listOf(provider("NBP", pivot = "PLN")),
            coverage = listOf(coverage("NBP", "PLN")),
            fetchedAt = 2_000L,
        )

        assertEquals(2_000L, dao.fetchedAt())
        assertEquals(listOf("PLN"), dao.currenciesOfType(CurrencyTypeColumn.Fiat).map { it.code })
        assertTrue(dao.providersCovering("CHF").isEmpty())
        assertEquals(listOf("NBP"), dao.providers(listOf("NBP", "ECB")).map { it.providerKey })
    }

    @Test
    fun currenciesOfTypeKeepsTheOtherTypesOut() = runTest {
        dao.replaceAll(
            currencies = listOf(
                currency("PLN"),
                currency("BTC", CurrencyTypeColumn.Crypto),
                currency("XAU", CurrencyTypeColumn.Metal),
            ),
            providers = emptyList(),
            coverage = emptyList(),
            fetchedAt = 1_000L,
        )

        assertEquals(listOf("PLN"), dao.currenciesOfType(CurrencyTypeColumn.Fiat).map { it.code })
        assertEquals(listOf("BTC"), dao.currenciesOfType(CurrencyTypeColumn.Crypto).map { it.code })
    }

    @Test
    fun providersAreReadBackWithEveryColumnTheyWereStoredWith() = runTest {
        val stored = ProviderEntity(
            providerKey = "ECB",
            name = "European Central Bank",
            rateType = "reference rate",
            pivotCurrency = "EUR",
            cadence = CadenceColumn.Daily,
            latestDate = "2026-11-17",
            dataUrl = "https://ecb.example.invalid",
        )

        dao.replaceAll(
            currencies = emptyList(),
            providers = listOf(stored),
            coverage = emptyList(),
            fetchedAt = 1_000L,
        )

        assertEquals(stored, dao.providers(listOf("ECB")).single())
    }

    private fun currency(
        code: String,
        type: CurrencyTypeColumn = CurrencyTypeColumn.Fiat,
    ) = CurrencyEntity(code = code, name = code, symbol = code, type = type)

    private fun provider(key: String, pivot: String) = ProviderEntity(
        providerKey = key,
        name = key,
        rateType = null,
        pivotCurrency = pivot,
        cadence = CadenceColumn.Daily,
        latestDate = null,
        dataUrl = null,
    )

    private fun coverage(key: String, code: String) =
        ProviderCoverageEntity(providerKey = key, code = code)
}
