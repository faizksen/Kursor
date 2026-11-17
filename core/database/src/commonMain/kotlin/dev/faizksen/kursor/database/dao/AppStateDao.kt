package dev.faizksen.kursor.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import dev.faizksen.kursor.database.entities.AppStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppStateDao {

    @Query("""
        SELECT COUNT(*) > 0 FROM app_state
        WHERE id = ${AppStateEntity.SINGLE_ROW} AND defaultsSeededAt IS NOT NULL
    """)
    suspend fun defaultsSeeded(): Boolean

    @Query("""
        SELECT COUNT(*) > 0 FROM app_state
        WHERE id = ${AppStateEntity.SINGLE_ROW} AND defaultsSeededAt IS NOT NULL
    """)
    fun observeDefaultsSeeded(): Flow<Boolean>

    @Query("""
        INSERT INTO app_state (id, defaultsSeededAt)
        VALUES (${AppStateEntity.SINGLE_ROW}, :seededAt)
        ON CONFLICT(id) DO UPDATE SET defaultsSeededAt = excluded.defaultsSeededAt
    """)
    suspend fun markDefaultsSeeded(seededAt: Long)
}
