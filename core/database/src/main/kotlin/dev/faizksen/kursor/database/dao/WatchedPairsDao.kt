package dev.faizksen.kursor.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.faizksen.kursor.database.entities.WatchedPairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedPairsDao {
    @Query("""
        SELECT * FROM watched_pair ORDER BY position ASC
    """)
    fun observeAll(): Flow<List<WatchedPairEntity>>

    @Query("""
        INSERT OR IGNORE INTO watched_pair (base, quote, position, addedAt)
        VALUES(
            :base, :quote,
            (SELECT COALESCE(MAX(position), -1) + 1 FROM watched_pair),
            :addedAt
        )
    """)
    suspend fun insertAtEnd(base: String, quote: String, addedAt: Long): Long

    @Query("""
        SELECT id FROM watched_pair WHERE base = :base AND quote = :quote
    """)
    suspend fun findId(base: String, quote: String): Long?

    @Transaction
    suspend fun addAtEnd(base: String, quote: String, addedAt: Long): PairInsertResult {
        val rowId = insertAtEnd(base, quote, addedAt)
        return if (rowId != IGNORED) {
            PairInsertResult(id = rowId, alreadyExisted = false)
        } else {
            PairInsertResult(id = checkNotNull(findId(base, quote)), alreadyExisted = true)
        }
    }

    private companion object {
        const val IGNORED = -1L
    }
}

data class PairInsertResult(
    val id: Long,
    val alreadyExisted: Boolean,
)