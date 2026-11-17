package dev.faizksen.kursor.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import dev.faizksen.kursor.database.consts.CurrencyTypeColumn
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.entities.RateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MigrateToProviderPerPairTest {

    private val path = temporaryDatabasePath("kursor-migration-test")

    @BeforeTest
    fun setUp() {
        deleteDatabaseFiles(path)
    }

    @AfterTest
    fun tearDown() {
        deleteDatabaseFiles(path)
    }

    @Test
    fun theSeededPairsKeepTheirIssuerAcrossTheMigration() = runTest {
        schemaThree {
            execSQL(
                "INSERT INTO watched_pair (base, quote, position, addedAt) VALUES " +
                    "('EUR', 'PLN', 0, 1000), ('EUR', 'USD', 1, 1000), ('EUR', 'RUB', 2, 1000)"
            )
        }

        migrated { database ->
            assertEquals(
                mapOf("PLN" to "NBP", "USD" to "ECB", "RUB" to "CBR"),
                database.watchedPairsDao.getAll().associate { pair -> pair.quote to pair.provider },
            )
        }
    }

    @Test
    fun aPairTheAppNeverSeededIsLeftWithoutAnIssuer() = runTest {
        schemaThree {
            execSQL(
                "INSERT INTO watched_pair (base, quote, position, addedAt) " +
                    "VALUES ('CHF', 'JPY', 0, 1000)"
            )
        }

        migrated { database ->
            assertEquals("", database.watchedPairsDao.getAll().single().provider)
        }
    }

    @Test
    fun theBlendedRatesAreDroppedSoTheNextSyncRefillsThemPerProvider() = runTest {
        schemaThree {
            execSQL(
                "INSERT INTO rate (base, quote, date, sourceId, value, fetchedAt) " +
                    "VALUES ('EUR', 'PLN', '2026-11-14', 'FRANKFURTER', '4.4016', 1000)"
            )
        }

        migrated { database ->
            assertTrue(database.ratesDao.observeLatestRates().first().isEmpty())
        }
    }

    @Test
    fun theSamePairThroughTwoIssuersFitsTheMigratedIndex() = runTest {
        schemaThree {
            execSQL(
                "INSERT INTO watched_pair (base, quote, position, addedAt) " +
                    "VALUES ('EUR', 'RUB', 0, 1000)"
            )
        }

        migrated { database ->
            val added = database.watchedPairsDao.addAtEnd(
                base = "EUR",
                quote = "RUB",
                provider = "ECB",
                addedAt = 2_000L,
            )

            assertFalse(added.alreadyExisted)
            assertEquals(2, database.watchedPairsDao.getAll().size)
        }
    }

    @Test
    fun theCatalogueSurvivesTheMigrationUntouched() = runTest {
        schemaThree {
            execSQL("INSERT INTO currency (code, name, symbol, type) VALUES ('PLN', 'Zloty', 'zl', 'fiat')")
            execSQL("INSERT INTO catalogue_meta (id, fetchedAt) VALUES (0, 5000)")
        }

        migrated { database ->
            assertEquals(5_000L, database.catalogueDao.fetchedAt())
            assertEquals(
                listOf("PLN"),
                database.catalogueDao.currenciesOfType(CurrencyTypeColumn.Fiat).map { it.code },
            )
        }
    }

    @Test
    fun anExistingDatabaseIsTreatedAsAlreadySeeded() = runTest {
        schemaThree {
            execSQL(
                "INSERT INTO watched_pair (base, quote, position, addedAt) " +
                    "VALUES ('EUR', 'PLN', 0, 1000)"
            )
        }

        migrated { database ->
            assertTrue(database.appStateDao.defaultsSeeded())
        }
    }

    @Test
    fun aRateCarriesItsProviderAfterTheMigration() = runTest {
        schemaThree {}

        migrated { database ->
            database.ratesDao.upsertAll(
                listOf(
                    rate(provider = "NBP", value = "4.4016"),
                    rate(provider = "ECB", value = "4.4113"),
                )
            )

            val stored = database.ratesDao.observeLatestRates().first()

            assertEquals(2, stored.size)
            assertEquals(setOf("NBP", "ECB"), stored.map { it.provider }.toSet())
            assertNull(stored.firstOrNull { it.provider.isEmpty() })
        }
    }

    private fun rate(provider: String, value: String) = RateEntity(
        base = "EUR",
        quote = "PLN",
        date = "2026-11-17",
        sourceId = SourceIdColumn.Frankfurter,
        provider = provider,
        value = value,
        fetchedAt = 1_000L,
    )

    private fun schemaThree(fill: SQLiteConnection.() -> Unit) {
        val driver = BundledSQLiteDriver()
        val connection = driver.open(path)

        try {
            SCHEMA_THREE.forEach { statement -> connection.execSQL(statement) }
            connection.execSQL("PRAGMA user_version = 3")
            connection.fill()
        } finally {
            connection.close()
        }
    }

    private suspend fun migrated(assertions: suspend (KursorDatabase) -> Unit) {
        val database = fileBuilder(path)
            .addMigrations(MigrateToProviderPerPair, MigrateSeedingOutOfTheDatabase)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()

        try {
            assertions(database)
        } finally {
            database.close()
        }
    }

    private companion object {
        val SCHEMA_THREE = listOf(
            "CREATE TABLE IF NOT EXISTS `rate` (`base` TEXT NOT NULL, `quote` TEXT NOT NULL, `date` TEXT NOT NULL, `sourceId` TEXT NOT NULL, `value` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`base`, `quote`, `date`, `sourceId`))",
            "CREATE INDEX IF NOT EXISTS `index_rate_base_quote_date` ON `rate` (`base` ASC, `quote` ASC, `date` DESC)",
            "CREATE TABLE IF NOT EXISTS `watched_pair` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `base` TEXT NOT NULL, `quote` TEXT NOT NULL, `position` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL)",
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_watched_pair_base_quote` ON `watched_pair` (`base`, `quote`)",
            "CREATE TABLE IF NOT EXISTS `sync_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sourceId` TEXT NOT NULL, `trigger` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `finishedAt` INTEGER NOT NULL, `result` TEXT NOT NULL, `errorKind` TEXT, `updatedCount` INTEGER, `attempt` INTEGER NOT NULL)",
            "CREATE INDEX IF NOT EXISTS `index_sync_log_finishedAt` ON `sync_log` (`finishedAt` DESC)",
            "CREATE TABLE IF NOT EXISTS `currency` (`code` TEXT NOT NULL, `name` TEXT NOT NULL, `symbol` TEXT NOT NULL, `type` TEXT NOT NULL, PRIMARY KEY(`code`))",
            "CREATE TABLE IF NOT EXISTS `provider` (`providerKey` TEXT NOT NULL, `name` TEXT NOT NULL, `rateType` TEXT, `pivotCurrency` TEXT NOT NULL, `cadence` TEXT NOT NULL, `latestDate` TEXT, `dataUrl` TEXT, PRIMARY KEY(`providerKey`))",
            "CREATE TABLE IF NOT EXISTS `provider_coverage` (`providerKey` TEXT NOT NULL, `code` TEXT NOT NULL, PRIMARY KEY(`providerKey`, `code`))",
            "CREATE INDEX IF NOT EXISTS `index_provider_coverage_code` ON `provider_coverage` (`code`)",
            "CREATE TABLE IF NOT EXISTS `catalogue_meta` (`id` INTEGER NOT NULL, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
            "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)",
            "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, '$SCHEMA_THREE_HASH')",
        )

        const val SCHEMA_THREE_HASH = "63d2b3e5faa984f40b646efa9f62a29c"
    }
}
