package dev.faizksen.kursor.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import dev.faizksen.kursor.database.entities.SyncAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncAttemptDao {

    @Insert
    suspend fun insertAll(attempts: List<SyncAttemptEntity>)

    @Query("SELECT * FROM sync_attempt WHERE runId = :runId ORDER BY startedAt ASC")
    fun observeForRun(runId: Long): Flow<List<SyncAttemptEntity>>

    @Query("SELECT COUNT(*) FROM sync_attempt")
    suspend fun count(): Int
}
