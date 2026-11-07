package dev.faizksen.kursor.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.entities.RateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatesDao {

    @Transaction
    suspend fun upsertAll(rates: List<RateEntity>) {
        rates.forEach { rate ->
            upsert(
                base = rate.base,
                quote = rate.quote,
                date = rate.date,
                sourceId = rate.sourceId,
                value = rate.value,
                fetchedAt = rate.fetchedAt,
            )
        }
    }

    // Written out instead of @Upsert or @Insert(onConflict = REPLACE), both of which
    // have drawbacks this avoids. REPLACE deletes the conflicting row before inserting,
    // which fires ON DELETE CASCADE: harmless while nothing references `rate`, a silent
    // data loss the day something does. @Upsert does not delete, but Room implements it
    // as insert-then-catch and decides whether the failure was a key conflict by reading
    // the exception message, which is fragile.
    // ON CONFLICT DO UPDATE needs SQLite 3.24; the bundled driver ships 3.50 everywhere,
    // so it is available even on the oldest Android we support, whose own SQLite is 3.22.
    @Query(
        """
        INSERT INTO rate (base, quote, date, sourceId, value, fetchedAt)
        VALUES (:base, :quote, :date, :sourceId, :value, :fetchedAt)
        ON CONFLICT(base, quote, date, sourceId)
        DO UPDATE SET value = excluded.value, fetchedAt = excluded.fetchedAt
    """
    )
    suspend fun upsert(
        base: String,
        quote: String,
        date: String,
        sourceId: SourceIdColumn,
        value: String,
        fetchedAt: Long,
    )

    @Query(
        """
        SELECT * FROM rate r
        WHERE r.date = (
            SELECT MAX(date) FROM rate
            WHERE base = r.base AND quote = r.quote AND sourceId = r.sourceId
        )
    """
    )
    fun observeLatestRates(): Flow<List<RateEntity>>
}
