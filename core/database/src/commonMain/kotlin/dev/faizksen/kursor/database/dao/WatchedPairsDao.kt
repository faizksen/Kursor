package dev.faizksen.kursor.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import dev.faizksen.kursor.database.entities.WatchedPairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedPairsDao {
    @Query("""
        SELECT * FROM watched_pair
    """)
    suspend fun getAll(): List<WatchedPairEntity>

    @Query("""
        SELECT * FROM watched_pair ORDER BY position ASC
    """)
    fun observeAll(): Flow<List<WatchedPairEntity>>

    @Query("""
        INSERT OR IGNORE INTO watched_pair (base, quote, provider, position, addedAt)
        VALUES(
            :base, :quote, :provider,
            (SELECT COALESCE(MAX(position), -1) + 1 FROM watched_pair),
            :addedAt
        )
    """)
    suspend fun insertAtEnd(
        base: String,
        quote: String,
        provider: String,
        addedAt: Long,
    ): Long

    @Query("""
        SELECT id FROM watched_pair
        WHERE base = :base AND quote = :quote AND provider = :provider
    """)
    suspend fun findId(base: String, quote: String, provider: String): Long?

    @Query("""
        UPDATE watched_pair SET provider = :provider WHERE id = :id
    """)
    suspend fun setProvider(id: Long, provider: String)

    @Query("""
        DELETE FROM watched_pair WHERE id = :id
    """)
    suspend fun deleteById(id: Long)

    @Insert
    suspend fun insert(pair: WatchedPairEntity)

    @Transaction
    suspend fun addAtEnd(
        base: String,
        quote: String,
        provider: String,
        addedAt: Long,
    ): PairInsertResult {
        val rowId = insertAtEnd(base, quote, provider, addedAt)
        return if (rowId != IGNORED) {
            PairInsertResult(id = rowId, alreadyExisted = false)
        } else {
            PairInsertResult(id = checkNotNull(findId(base, quote, provider)), alreadyExisted = true)
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
