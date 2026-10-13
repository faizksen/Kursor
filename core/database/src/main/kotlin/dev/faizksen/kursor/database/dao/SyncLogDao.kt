package dev.faizksen.kursor.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.faizksen.kursor.database.consts.SYNC_SUCCESS
import dev.faizksen.kursor.database.entities.SyncLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncLogDao {
    @Insert
    suspend fun insert(entry: SyncLogEntity): Long

    @Query("SELECT * FROM sync_log ORDER BY finishedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SyncLogEntity>>

    @Query("SELECT * FROM sync_log WHERE result = '$SYNC_SUCCESS' ORDER BY finishedAt DESC LIMIT 1")
    fun observeLastSuccess(): Flow<SyncLogEntity?>

    @Query("DELETE FROM sync_log WHERE finishedAt < :invalidateOlderThan")
    suspend fun deleteOlderThan(invalidateOlderThan: Long)
}