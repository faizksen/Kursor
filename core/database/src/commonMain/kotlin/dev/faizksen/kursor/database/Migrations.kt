package dev.faizksen.kursor.database

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal object MigrateToProviderPerPair : Migration(3, 4) {

    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `watched_pair` ADD COLUMN `provider` TEXT NOT NULL DEFAULT ''")
        connection.execSQL("DROP INDEX IF EXISTS `index_watched_pair_base_quote`")
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_watched_pair_base_quote_provider` " +
                "ON `watched_pair` (`base`, `quote`, `provider`)"
        )

        DEFAULT_WATCHED_PAIRS.forEach { pair ->
            connection.execSQL(
                "UPDATE `watched_pair` SET `provider` = '${pair.provider}' " +
                    "WHERE `base` = '${pair.base}' AND `quote` = '${pair.quote}' " +
                    "AND `provider` = ''"
            )
        }

        connection.execSQL("DROP TABLE `rate`")
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `rate` (`base` TEXT NOT NULL, `quote` TEXT NOT NULL, " +
                "`date` TEXT NOT NULL, `sourceId` TEXT NOT NULL, `provider` TEXT NOT NULL, " +
                "`value` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`base`, `quote`, `date`, `sourceId`, `provider`))"
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_rate_base_quote_date` " +
                "ON `rate` (`base` ASC, `quote` ASC, `date` DESC)"
        )
    }
}
