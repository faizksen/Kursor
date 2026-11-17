package dev.faizksen.kursor.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MigrateToProviderPerPairTest {

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        file = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getDatabasePath(MIGRATION_DATABASE),
        driver = BundledSQLiteDriver(),
        databaseClass = KursorDatabase::class,
        databaseFactory = { KursorDatabaseConstructor.initialize() },
    )

    @Test
    fun theSeededPairsKeepTheirIssuerAcrossTheMigration() = runTest {
        helper.createDatabase(version = 3).use { old ->
            old.execSQL(
                "INSERT INTO watched_pair (base, quote, position, addedAt) " +
                    "VALUES ('EUR', 'PLN', 0, 1000), ('EUR', 'USD', 1, 1000), ('EUR', 'RUB', 2, 1000)"
            )
        }

        migrated().use { migrated ->
            val providers = mutableMapOf<String, String>()
            migrated.prepare("SELECT quote, provider FROM watched_pair").use { statement ->
                while (statement.step()) {
                    providers[statement.getText(0)] = statement.getText(1)
                }
            }

            assertEquals(mapOf("PLN" to "NBP", "USD" to "ECB", "RUB" to "CBR"), providers.toMap())
        }
    }

    @Test
    fun aPairTheAppNeverSeededIsLeftWithoutAnIssuer() = runTest {
        helper.createDatabase(version = 3).use { old ->
            old.execSQL(
                "INSERT INTO watched_pair (base, quote, position, addedAt) " +
                    "VALUES ('CHF', 'JPY', 0, 1000)"
            )
        }

        migrated().use { migrated ->
            migrated.prepare("SELECT provider FROM watched_pair").use { statement ->
                assertTrue(statement.step())
                assertEquals("", statement.getText(0))
            }
        }
    }

    @Test
    fun theBlendedRatesAreDroppedSoTheNextSyncRefillsThemPerProvider() = runTest {
        helper.createDatabase(version = 3).use { old ->
            old.execSQL(
                "INSERT INTO rate (base, quote, date, sourceId, value, fetchedAt) " +
                    "VALUES ('EUR', 'PLN', '2026-11-14', 'FRANKFURTER', '4.4016', 1000)"
            )
        }

        migrated().use { migrated ->
            migrated.prepare("SELECT COUNT(*) FROM rate").use { statement ->
                assertTrue(statement.step())
                assertEquals(0, statement.getInt(0))
            }
        }
    }

    @Test
    fun theSamePairThroughTwoIssuersFitsTheMigratedIndex() = runTest {
        helper.createDatabase(version = 3).use { old ->
            old.execSQL(
                "INSERT INTO watched_pair (base, quote, position, addedAt) " +
                    "VALUES ('EUR', 'RUB', 0, 1000)"
            )
        }

        migrated().use { migrated ->
            migrated.execSQL(
                "INSERT INTO watched_pair (base, quote, provider, position, addedAt) " +
                    "VALUES ('EUR', 'RUB', 'ECB', 1, 2000)"
            )

            migrated.prepare("SELECT COUNT(*) FROM watched_pair").use { statement ->
                assertTrue(statement.step())
                assertEquals(2, statement.getInt(0))
            }
        }
    }

    private suspend fun migrated() = helper.runMigrationsAndValidate(
        version = 4,
        migrations = listOf(MigrateToProviderPerPair),
    )

    private companion object {
        const val MIGRATION_DATABASE = "migration-test"
    }
}
