package dev.faizksen.kursor.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.faizksen.kursor.database.consts.ErrorColumn
import dev.faizksen.kursor.database.consts.SourceIdColumn
import dev.faizksen.kursor.database.consts.SyncResultColumn
import dev.faizksen.kursor.database.consts.TriggerColumn

@Entity(
    tableName = "sync_log",
    indices = [Index(value = ["finishedAt"], orders = [Index.Order.DESC])]
)
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: SourceIdColumn,
    val trigger: TriggerColumn,
    val startedAt: Long,
    val finishedAt: Long,
    val result: SyncResultColumn,
    val errorKind: ErrorColumn?,
    val updatedCount: Int?,
    val attempt: Int
)
